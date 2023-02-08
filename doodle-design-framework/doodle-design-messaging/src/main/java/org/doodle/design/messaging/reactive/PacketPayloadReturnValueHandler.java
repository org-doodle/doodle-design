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
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.reactive.AbstractEncoderMethodReturnValueHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PacketPayloadReturnValueHandler extends AbstractEncoderMethodReturnValueHandler {

  public PacketPayloadReturnValueHandler(
      List<Encoder<?>> encoders, ReactiveAdapterRegistry registry) {
    super(encoders, registry);
  }

  @Override
  protected Mono<Void> handleEncodedContent(
      Flux<DataBuffer> encodedContent, MethodParameter returnType, Message<?> message) {
    return Mono.empty();
  }

  @Override
  protected Mono<Void> handleNoContent(MethodParameter returnType, Message<?> message) {
    return Mono.empty();
  }
}
