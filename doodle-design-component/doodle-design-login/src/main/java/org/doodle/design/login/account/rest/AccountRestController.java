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
package org.doodle.design.login.account.rest;

import static org.doodle.design.login.LoginRestMapping.ACCOUNT_AUTH_MAPPING;
import static org.doodle.design.login.LoginRestMapping.ACCOUNT_CREATE_MAPPING;
import static org.doodle.design.login.LoginRestMapping.ACCOUNT_MAPPING;

import java.util.Objects;
import org.doodle.design.common.CommonResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ACCOUNT_MAPPING)
public abstract class AccountRestController<
        AccountCreateRequestT extends AccountCreateRequest,
        AccountCreateResponseT extends AccountCreateResponse,
        AccountAuthRequestT extends AccountAuthRequest,
        AccountLoginResponseT extends AccountAuthResponse>
    implements AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT>,
        AccountAuthOperation<AccountAuthRequestT, AccountLoginResponseT> {

  protected final AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT>
      createOperation;
  protected final AccountAuthOperation<AccountAuthRequestT, AccountLoginResponseT> authOperation;

  public AccountRestController(
      AccountCreateOperation<AccountCreateRequestT, AccountCreateResponseT> createOperation,
      AccountAuthOperation<AccountAuthRequestT, AccountLoginResponseT> authOperation) {
    this.createOperation = Objects.requireNonNull(createOperation);
    this.authOperation = Objects.requireNonNull(authOperation);
  }

  @PostMapping(ACCOUNT_CREATE_MAPPING)
  @Override
  public CommonResult<AccountCreateResponseT> create(AccountCreateRequestT request) {
    return this.createOperation.create(request);
  }

  @PostMapping(ACCOUNT_AUTH_MAPPING)
  @Override
  public CommonResult<AccountLoginResponseT> auth(AccountAuthRequestT request) {
    return this.authOperation.auth(request);
  }
}
