/*
 * Copyright 2022 DtmOyaji.
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
package org.tiny.wicket.onelogin;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

/**
 *
 * @author DtmOyaji
 */
public class SamlSession extends AuthenticatedWebSession {

    private final Request request;

    private SamlAuthInfo SamlAuthInfo;

    private Roles roles = new Roles();

    public SamlSession(Request request) {
        super(request);
        this.request = request;
    }

    @Override
    protected boolean authenticate(String userName, String password) {

        HttpServletRequest servletRequest = (HttpServletRequest) this.request.getContainerRequest();

        SamlProcess checkLogin = new SamlProcess(
                servletRequest,
                null,
                SamlProcess.MODE_CHECKLOGIN
        );

        if (checkLogin.getStatus() == SamlAuthInfo.STATUS_NOTAUTHENTICATED) {
            checkLogin = new SamlProcess(servletRequest, null, SamlProcess.MODE_LOGIN);
        }

        if (checkLogin.getStatus() == SamlAuthInfo.STATUS_AUTHENTICATED) {
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        return this.roles;
    }

    public void setSamlAuthInfo(SamlAuthInfo info) {
        this.SamlAuthInfo = info;
        if (this.SamlAuthInfo.getAttributes() != null) {
            List<String> attrs = this.SamlAuthInfo.getAttribute("Role");
            if (attrs != null) {
                this.getRoles().addAll(attrs);
            }else{
                this.getRoles().clear();
            }
        }
        //System.out.println(this.SamlAuthInfo.toString());
    }

    public SamlAuthInfo getSamlAuthInfo() {
        return this.SamlAuthInfo;
    }

}
