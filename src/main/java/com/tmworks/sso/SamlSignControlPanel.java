/*
 * Copyright 2022 MURAKAMI Takahiro <daianji@gmail.com>.
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author MURAKAMI Takahiro <daianji@gmail.com>
 */
public class SamlSignControlPanel extends Panel {

    private final Form formLogout;

    private final Form formLogin;

    private final Button logoutButton;
    private final Button loginButton;

    private final Label lblInfo;

    private final PageParameters parameters;

    private SamlAuthInfo authInfo;

    public SamlSignControlPanel(String id, PageParameters parameters) {
        super(id);

        this.parameters = parameters;

        this.formLogout = new Form("logoutForm");
        this.add(this.formLogout);

        this.logoutButton = new Button("logoutButton", Model.of("ログアウト")) {
            @Override
            public void onSubmit() {
                this.setResponsePage(SamlLogoutPage.class, SamlSignControlPanel.this.parameters);
            }
        };
        this.formLogout.add(this.logoutButton);

        this.formLogin = new Form("loginForm");
        this.add(this.formLogin);

        this.loginButton = new Button("loginButton", Model.of("ログイン")) {
            @Override
            public void onSubmit() {
                this.setResponsePage(SamlSigninPage.class, SamlSignControlPanel.this.parameters);
            }
        };
        this.formLogin.add(this.loginButton);

        this.lblInfo = new Label("info", Model.of("まだログインしていません"));
        this.add(this.lblInfo);

    }

    public void showStatus(WebPage parent) {
        AuthenticatedSession session = (AuthenticatedSession) parent.getSession();
        SamlProcess sprocess = new SamlProcess(parent, SamlProcess.MODE_CHECKLOGIN);
        if (sprocess.getStatus() == SamlAuthInfo.STATUS_AUTHENTICATED) {
            this.authInfo = sprocess.getAuthInfo();

            session.setSamlAuthInfo(this.authInfo);
            Roles roles = session.getRoles();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    roles.toString()
                    );

            String data = authInfo.getAttributes().toString();
            data += "<br />[session-index]:" + this.authInfo.getSessionIndex();
            data += "<br />[nameid]:" + this.authInfo.getNameId();
            this.lblInfo.setDefaultModel(Model.of(data));
            this.loginButton.setVisible(false);
        } else {
            this.authInfo = null;
            this.logoutButton.setVisible(false);
        }
    }

    public SamlAuthInfo getAuth() {
        return this.authInfo;
    }

}
