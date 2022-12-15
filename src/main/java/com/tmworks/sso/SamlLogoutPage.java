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

import com.tmworks.SamlMainPage;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author bythe
 */
public class SamlLogoutPage extends WebPage {

    Roles myRole;

    public SamlLogoutPage(final PageParameters params) {
        super(params);

        SamlProcess process = new SamlProcess(this, SamlProcess.MODE_CHECKLOGIN);
        if (process.getStatus() == SamlAuthInfo.STATUS_AUTHENTICATED) {
            SamlAuthedSession authSession = (SamlAuthedSession) this.getSession();

            String responseXml = authSession.getSamlAuthInfo().getNameId();
            responseXml = authSession.getSamlAuthInfo().getSessionIndex();

            new SamlProcess(this, SamlProcess.MODE_LOGOUT);

            this.setResponsePage(SamlMainPage.class);

        }

    }
}
