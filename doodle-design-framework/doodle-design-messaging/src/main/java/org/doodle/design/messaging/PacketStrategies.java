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

import java.util.List;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;
import org.springframework.util.RouteMatcher;

public interface PacketStrategies {

  List<Encoder<?>> encoders();

  @SuppressWarnings("unchecked")
  default <T> Encoder<T> encoder(ResolvableType elementType, @Nullable MimeType mimeType) {
    for (Encoder<?> encoder : encoders()) {
      if (encoder.canEncode(elementType, mimeType)) {
        return (Encoder<T>) encoder;
      }
    }
    throw new IllegalArgumentException("No encoder for " + elementType);
  }

  List<Decoder<?>> decoders();

  @SuppressWarnings("unchecked")
  default <T> Decoder<T> decoder(ResolvableType elementType, @Nullable MimeType mimeType) {
    for (Decoder<?> decoder : decoders()) {
      if (decoder.canDecode(elementType, mimeType)) {
        return (Decoder<T>) decoder;
      }
    }
    throw new IllegalArgumentException("No decoder for " + elementType);
  }

  RouteMatcher routeMatcher();

  DataBufferFactory dataBufferFactory();

  PacketMetadataExtractor metadataExtractor();

  interface Builder {

    Builder encoder(Encoder<?>... encoder);

    Builder decoder(Decoder<?>... decoder);

    Builder routeMatcher(@Nullable RouteMatcher routeMatcher);

    Builder dataBufferFactory(@Nullable DataBufferFactory dataBufferFactory);

    Builder metadataExtractor(@Nullable PacketMetadataExtractor metadataExtractor);

    PacketStrategies build();
  }
}
