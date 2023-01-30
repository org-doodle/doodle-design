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
package org.doodle.design.messaging;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.DestinationPatternsMessageCondition;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.reactive.MessageMappingMessageHandler;
import org.springframework.messaging.handler.invocation.reactive.AbstractEncoderMethodReturnValueHandler;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.SimpleRouteMatcher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MessageMappingMessageHandlerTests {

  @Test
  public void handleString() {
    MessageMappingMessageHandler messageHandler = initMessageHandler();
    Message<?> message = message("11.11", "abcdef");
    messageHandler.handleMessage(message).block(Duration.ofSeconds(5));
    verifyOutputContent(Collections.singletonList("abcdef::response"));
  }

  private Message<?> message(String destination, String... content) {
    Flux<DataBuffer> payload = Flux.fromIterable(Arrays.asList(content)).map(this::toDataBuffer);
    MessageHeaderAccessor headers = new MessageHeaderAccessor();
    headers.setLeaveMutable(true);
    headers.setHeader(
        DestinationPatternsMessageCondition.LOOKUP_DESTINATION_HEADER,
        new SimpleRouteMatcher(new AntPathMatcher()).parseRoute(destination));
    return MessageBuilder.createMessage(payload, headers.getMessageHeaders());
  }

  private DataBuffer toDataBuffer(String payload) {
    return DefaultDataBufferFactory.sharedInstance.wrap(payload.getBytes(UTF_8));
  }

  private void verifyOutputContent(List<String> expected) {}

  MessageMappingMessageHandler initMessageHandler() {

    List<Decoder<?>> decoders = Collections.singletonList(StringDecoder.allMimeTypes());
    List<Encoder<?>> encoders = Collections.singletonList(CharSequenceEncoder.allMimeTypes());

    ReactiveAdapterRegistry registry = ReactiveAdapterRegistry.getSharedInstance();

    PropertySource<?> source =
        new MapPropertySource("test", Collections.singletonMap("path", "path123"));

    StaticApplicationContext context = new StaticApplicationContext();
    context.getEnvironment().getPropertySources().addFirst(source);
    context.registerSingleton("testController", TestController.class);
    context.refresh();

    MessageMappingMessageHandler messageHandler = new MessageMappingMessageHandler();
    messageHandler
        .getReturnValueHandlerConfigurer()
        .addCustomHandler(
            new AbstractEncoderMethodReturnValueHandler(encoders, registry) {
              @Override
              protected Mono<Void> handleEncodedContent(
                  Flux<DataBuffer> encodedContent, MethodParameter returnType, Message<?> message) {
                return encodedContent.then();
              }

              @Override
              protected Mono<Void> handleNoContent(MethodParameter returnType, Message<?> message) {
                return Mono.empty();
              }
            });
    messageHandler.setApplicationContext(context);
    messageHandler.setEmbeddedValueResolver(new EmbeddedValueResolver(context.getBeanFactory()));
    messageHandler.setDecoders(decoders);
    messageHandler.afterPropertiesSet();

    return messageHandler;
  }

  @Controller
  static class TestController {
    @MessageMapping("11.11") // 11.11
    public String handleString(String payload) {
      return payload + "::response";
    }
  }
}
