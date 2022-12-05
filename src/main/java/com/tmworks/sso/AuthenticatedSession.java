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

import com.tmworks.sso.SamlProcess;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

/**
 *
 * @author bythe
 */
public class AuthenticatedSession extends AuthenticatedWebSession {

    private final Request request;

    Roles myRoles;

    public AuthenticatedSession(Request request) {
        super(request);
        this.request = request;
        this.myRoles = new Roles();
    }

    @Override
    protected boolean authenticate(String userName, String password) {

        HttpServletRequest request = (HttpServletRequest) this.request.getContainerRequest();

        SamlProcess checkLogin = new SamlProcess(
                request,
                null,
                SamlProcess.MODE_CHECKLOGIN
        );

        if (checkLogin.getStatus() == SamlProcess.STATUS_NOTAUTHENTICATED) {
            checkLogin = new SamlProcess(request, null, SamlProcess.MODE_LOGIN);
        }

        if (checkLogin.getStatus() == SamlProcess.STATUS_AUTHENTICATED) {
            this.myRoles.add("user");
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        return this.myRoles;
    }

    public void putRoles(String role) {
        if (!this.myRoles.contains(role)) {
            this.myRoles.add(role);
        }
    }

}
