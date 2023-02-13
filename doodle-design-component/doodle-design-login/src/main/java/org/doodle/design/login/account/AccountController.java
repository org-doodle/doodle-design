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
package org.doodle.design.login.account;

import java.util.Objects;
import org.doodle.design.common.CommonResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("login.account")
public abstract class AccountController<
        AccountCreateRequestT extends AccountCreateRequest,
        AccountCreateResponseT extends AccountCreateResponse,
        AccountLoginRequestT extends AccountAuthRequest,
        AccountLoginResponseT extends AccountAuthResponse>
    implements AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT>,
        AccountAuthOperation<AccountLoginRequestT, AccountLoginResponseT> {

  protected final AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT>
      createOperation;
  protected final AccountAuthOperation<AccountLoginRequestT, AccountLoginResponseT> authOperation;

  public AccountController(
      AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT> createOperation,
      AccountAuthOperation<AccountLoginRequestT, AccountLoginResponseT> authOperation) {
    this.createOperation = Objects.requireNonNull(createOperation);
    this.authOperation = Objects.requireNonNull(authOperation);
  }

  @PostMapping(path = "create")
  @Override
  public CommonResult<AccountCreateResponseT> create(AccountAuthRequest request) {
    return this.createOperation.create(request);
  }

  @PostMapping(path = "auth")
  @Override
  public CommonResult<AccountLoginResponseT> auth(AccountAuthRequest request) {
    return this.authOperation.auth(request);
  }
}
