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

import org.doodle.design.messaging.PacketRequester;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.HandlerMethodArgumentResolver;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

public class PacketRequesterMethodArgumentResolver implements HandlerMethodArgumentResolver {
  public static final String PACKET_REQUESTER_HEADER = "packetRequester";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    Class<?> type = parameter.getParameterType();
    return PacketRequester.class.equals(type);
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, Message<?> message) {
    Object headerValue = message.getHeaders().get(PACKET_REQUESTER_HEADER);
    Assert.notNull(headerValue, "Missing '" + PACKET_REQUESTER_HEADER + "'");

    Assert.isInstanceOf(
        PacketRequester.class, headerValue, "Expected header value of type PacketRequester");
    PacketRequester requester = (PacketRequester) headerValue;

    Class<?> type = parameter.getParameterType();
    if (PacketRequester.class.equals(type) || PacketRequester.class.isAssignableFrom(type)) {
      return Mono.just(requester);
    } else {
      return Mono.error(new IllegalArgumentException("Unexpected parameter type: " + parameter));
    }
  }
}
