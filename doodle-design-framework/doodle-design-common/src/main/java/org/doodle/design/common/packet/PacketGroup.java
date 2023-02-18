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
package org.doodle.design.common.packet;

public interface PacketGroup {
  short LOGIN = 1;
  short ROLE = 2;
  short PAYMENT = 3;
  short MAIL = 4;
  short BAG = 5;
  short TASK = 6;
  short RANK = 7;
  short FRIEND = 8;
  short GUILD = 9;
}
