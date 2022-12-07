/*
 * Copyright 2022 bythe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tmworks.sso;

import com.onelogin.saml2.Auth;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author bythe
 */
public class SamlSigninPage extends WebPage {

    Roles myRole;

    public SamlSigninPage(final PageParameters params) {
        super(params);

        // SAML処理
        SamlProcess checkLogin = new SamlProcess(
                this,
                SamlProcess.MODE_CHECKLOGIN
        );

        if (checkLogin.getStatus() == SamlProcess.STATUS_NOTAUTHENTICATED) {
            checkLogin = new SamlProcess(this, SamlProcess.MODE_LOGIN);
        }

        if (checkLogin.getStatus() == SamlProcess.STATUS_AUTHENTICATED) {
            AuthenticatedSession session = (AuthenticatedSession) this.getSession();
            session.putRoles("user");
            Auth auth = checkLogin.getAuth();
            session.setAuth(auth);
        }
    }
}
