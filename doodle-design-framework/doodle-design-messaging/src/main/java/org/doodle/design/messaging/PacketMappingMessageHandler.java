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

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.codec.Encoder;
import org.springframework.messaging.handler.CompositeMessageCondition;
import org.springframework.messaging.handler.annotation.reactive.MessageMappingMessageHandler;

public class PacketMappingMessageHandler extends MessageMappingMessageHandler {

  @Getter @Setter private List<Encoder<?>> encoders = new ArrayList<>();

  @Override
  protected CompositeMessageCondition getCondition(AnnotatedElement element) {
    PacketMapping ann = AnnotatedElementUtils.findMergedAnnotation(element, PacketMapping.class);
    if (Objects.nonNull(ann) && ann.value() > 0 && ann.cmd() > 0) {
      return new CompositeMessageCondition();
    } else {
      return super.getCondition(element);
    }
  }
}
