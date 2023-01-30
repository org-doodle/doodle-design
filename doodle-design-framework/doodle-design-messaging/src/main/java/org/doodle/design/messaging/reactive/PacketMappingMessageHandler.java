/*
 * Copyright (c) 2022-present Doodle. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.doodle.design.messaging.reactive;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.doodle.design.messaging.PacketMapping;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.codec.Decoder;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.CompositeMessageCondition;
import org.springframework.messaging.handler.DestinationPatternsMessageCondition;
import org.springframework.messaging.handler.annotation.reactive.DestinationVariableMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.reactive.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.reactive.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.reactive.PayloadMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.AnnotationExceptionHandlerMethodResolver;
import org.springframework.messaging.handler.invocation.AbstractExceptionHandlerMethodResolver;
import org.springframework.messaging.handler.invocation.reactive.AbstractMethodMessageHandler;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodReturnValueHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.RouteMatcher;
import org.springframework.util.SimpleRouteMatcher;
import org.springframework.validation.Validator;

public class PacketMappingMessageHandler
    extends AbstractMethodMessageHandler<CompositeMessageCondition> {
  @Getter private final List<Decoder<?>> decoders = new ArrayList<>();
  @Nullable @Getter @Setter private Validator validator;
  @Nullable @Getter @Setter private RouteMatcher routeMatcher;

  @Getter @Setter
  private ConversionService conversionService = new DefaultFormattingConversionService();

  public PacketMappingMessageHandler() {
    setHandlerPredicate(type -> AnnotatedElementUtils.isAnnotated(type, Controller.class));
  }

  public void setDecoders(List<? extends Decoder<?>> decoders) {
    this.decoders.clear();
    this.decoders.addAll(decoders);
  }

  protected RouteMatcher obtainRouteMatcher() {
    RouteMatcher routeMatcher = getRouteMatcher();
    Assert.state(Objects.nonNull(routeMatcher), "No RouteMatcher set");
    return routeMatcher;
  }

  @Override
  public void afterPropertiesSet() {
    if (Objects.isNull(this.routeMatcher)) {
      AntPathMatcher pathMatcher = new AntPathMatcher();
      pathMatcher.setPathSeparator(".");
      this.routeMatcher = new SimpleRouteMatcher(pathMatcher);
    }

    super.afterPropertiesSet();
  }

  @Override
  protected List<? extends HandlerMethodArgumentResolver> initArgumentResolvers() {
    List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();
    ApplicationContext context = getApplicationContext();
    ConfigurableListableBeanFactory beanFactory =
        (context instanceof ConfigurableApplicationContext)
            ? ((ConfigurableApplicationContext) context).getBeanFactory()
            : null;
    resolvers.add(new HeaderMethodArgumentResolver(this.conversionService, beanFactory));
    resolvers.add(new HeadersMethodArgumentResolver());
    resolvers.add(new DestinationVariableMethodArgumentResolver(this.conversionService));
    resolvers.addAll(getArgumentResolverConfigurer().getCustomResolvers());
    resolvers.add(
        new PayloadMethodArgumentResolver(
            getDecoders(), this.validator, getReactiveAdapterRegistry(), true));
    return resolvers;
  }

  @Override
  protected List<? extends HandlerMethodReturnValueHandler> initReturnValueHandlers() {
    return Collections.emptyList();
  }

  @Override
  protected CompositeMessageCondition getMappingForMethod(Method method, Class<?> handlerType) {
    CompositeMessageCondition methodCondition = getCondition(method);
    if (Objects.nonNull(methodCondition)) {
      CompositeMessageCondition typeCondition = getCondition(handlerType);
      if (Objects.nonNull(typeCondition)) {
        return typeCondition.combine(methodCondition);
      }
    }
    return methodCondition;
  }

  @Nullable
  protected CompositeMessageCondition getCondition(AnnotatedElement element) {
    PacketMapping ann = AnnotatedElementUtils.findMergedAnnotation(element, PacketMapping.class);
    if (Objects.isNull(ann) || ann.inbound().value() == 0) {
      return null;
    }

    String[] patterns = processDestinations(ann.inbound());
    return new CompositeMessageCondition(
        new DestinationPatternsMessageCondition(patterns, obtainRouteMatcher()));
  }

  protected String[] processDestinations(PacketMapping.Inbound... inbound) {
    return Stream.of(inbound)
        .mapToInt(PacketMapping.Inbound::value)
        .mapToObj(String::valueOf)
        .toArray(String[]::new);
  }

  @Override
  protected Set<String> getDirectLookupMappings(CompositeMessageCondition mapping) {
    Set<String> results = new LinkedHashSet<>();
    for (String pattern :
        mapping.getCondition(DestinationPatternsMessageCondition.class).getPatterns()) {
      if (!obtainRouteMatcher().isPattern(pattern)) {
        results.add(pattern);
      }
    }
    return results;
  }

  @Override
  protected RouteMatcher.Route getDestination(Message<?> message) {
    return (RouteMatcher.Route)
        message.getHeaders().get(DestinationPatternsMessageCondition.LOOKUP_DESTINATION_HEADER);
  }

  @Override
  protected CompositeMessageCondition getMatchingMapping(
      CompositeMessageCondition mapping, Message<?> message) {
    return mapping.getMatchingCondition(message);
  }

  @Override
  protected Comparator<CompositeMessageCondition> getMappingComparator(Message<?> message) {
    return (condition1, condition2) -> condition1.compareTo(condition2, message);
  }

  @Override
  protected AbstractExceptionHandlerMethodResolver createExceptionMethodResolverFor(
      Class<?> beanType) {
    return new AnnotationExceptionHandlerMethodResolver(beanType);
  }
}
