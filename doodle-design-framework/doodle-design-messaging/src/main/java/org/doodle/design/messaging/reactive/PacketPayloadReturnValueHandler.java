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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.doodle.design.messaging.PacketPayload;
import org.doodle.design.messaging.PacketPayloadUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.AbstractEncoderMethodReturnValueHandler;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PacketPayloadReturnValueHandler extends AbstractEncoderMethodReturnValueHandler {

  public static final String RESPONSE_HEADER = "packetResponse";

  public PacketPayloadReturnValueHandler(
      List<Encoder<?>> encoders, ReactiveAdapterRegistry registry) {
    super(encoders, registry);
  }

  @Override
  protected Mono<Void> handleEncodedContent(
      Flux<DataBuffer> encodedContent, MethodParameter returnType, Message<?> message) {
    AtomicReference<Flux<PacketPayload>> responseRef = getResponseReference(message);
    Assert.notNull(responseRef, "Missing '" + RESPONSE_HEADER + "'");
    responseRef.set(encodedContent.map(PacketPayloadUtils::createPayload));
    return Mono.empty();
  }

  @Override
  protected Mono<Void> handleNoContent(MethodParameter returnType, Message<?> message) {
    AtomicReference<Flux<PacketPayload>> responseRef = getResponseReference(message);
    if (Objects.nonNull(responseRef)) {
      responseRef.set(Flux.empty());
    }
    return Mono.empty();
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private AtomicReference<Flux<PacketPayload>> getResponseReference(Message<?> message) {
    Object headerValue = message.getHeaders().get(RESPONSE_HEADER);
    Assert.state(
        headerValue == null || headerValue instanceof AtomicReference, "Expected AtomicReference");
    return (AtomicReference<Flux<PacketPayload>>) headerValue;
  }
}
