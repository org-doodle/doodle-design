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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.lang.Nullable;

public final class DefaultPacketPayload implements PacketPayload {

  private final ByteBuffer data;
  private final ByteBuffer metadata;

  private DefaultPacketPayload(ByteBuffer data, @Nullable ByteBuffer metadata) {
    this.data = data;
    this.metadata = metadata;
  }

  public static DefaultPacketPayload create(CharSequence data) {
    return create(StandardCharsets.UTF_8.encode(CharBuffer.wrap(data)), null);
  }

  public static DefaultPacketPayload create(CharSequence data, @Nullable CharSequence metadata) {
    return create(
        StandardCharsets.UTF_8.encode(CharBuffer.wrap(data)),
        Objects.nonNull(metadata)
            ? StandardCharsets.UTF_8.encode(CharBuffer.wrap(metadata))
            : null);
  }

  public static DefaultPacketPayload create(CharSequence data, Charset dataCharset) {
    return create(dataCharset.encode(CharBuffer.wrap(data)), null);
  }

  public static DefaultPacketPayload create(
      CharSequence data,
      Charset dataCharset,
      @Nullable CharSequence metadata,
      Charset metadataCharset) {
    return create(
        dataCharset.encode(CharBuffer.wrap(data)),
        Objects.nonNull(metadata) ? metadataCharset.encode(CharBuffer.wrap(metadata)) : null);
  }

  public static DefaultPacketPayload create(byte[] data) {
    return create(ByteBuffer.wrap(data));
  }

  public static DefaultPacketPayload create(byte[] data, @Nullable byte[] metadata) {
    return create(
        ByteBuffer.wrap(data), Objects.nonNull(metadata) ? ByteBuffer.wrap(metadata) : null);
  }

  public static DefaultPacketPayload create(ByteBuffer data) {
    return create(data, null);
  }

  public static DefaultPacketPayload create(ByteBuffer data, @Nullable ByteBuffer metadata) {
    return new DefaultPacketPayload(data, metadata);
  }

  public static DefaultPacketPayload create(ByteBuf data) {
    return create(data, null);
  }

  public static DefaultPacketPayload create(ByteBuf data, @Nullable ByteBuf metadata) {
    try {
      return create(toBytes(data), Objects.nonNull(metadata) ? toBytes(metadata) : null);
    } finally { // release buffer.
      data.release();
      if (Objects.nonNull(metadata)) {
        metadata.release();
      }
    }
  }

  private static byte[] toBytes(ByteBuf byteBuf) {
    byte[] bytes = new byte[byteBuf.readableBytes()];
    byteBuf.markReaderIndex();
    byteBuf.readBytes(bytes);
    byteBuf.resetReaderIndex();
    return bytes;
  }

  @Override
  public ByteBuf metadata() {
    return sliceMetadata();
  }

  @Override
  public ByteBuf sliceMetadata() {
    return Objects.nonNull(metadata) ? Unpooled.wrappedBuffer(metadata) : null;
  }

  @Override
  public ByteBuf data() {
    return sliceData();
  }

  @Override
  public ByteBuf sliceData() {
    return Objects.nonNull(data) ? Unpooled.wrappedBuffer(data) : null;
  }
}
