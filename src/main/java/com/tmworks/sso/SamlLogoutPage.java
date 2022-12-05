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
import javax.servlet.http.Cookie;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 *
 * @author bythe
 */
public class SamlLogoutPage extends WebPage {

    Roles myRole;

    public SamlLogoutPage(final PageParameters params) {
        super(params);

        StringValue responseXml = params.get("name_id");
        System.out.println(responseXml.toString());
        responseXml = params.get("id");
        System.out.println(responseXml.toString());

        SamlProcess sprocess = new SamlProcess(this, params, SamlProcess.MODE_LOGOUT);
        Auth auth = sprocess.getAuth();

        WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
        Cookie cookie = webRequest.getCookie("yourCookieName");

        this.getSession().invalidateNow();
        this.getSession().clear();
    }
}
