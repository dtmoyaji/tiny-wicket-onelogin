/*
 * Copyright 2022 DtmOyaji
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

import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author DtmOyaji
 */
public class SamlSignControlPanel extends Panel {

    private final Form signControlPanel;

    private final Button logoutButton;
    private final Button loginButton;

    private final Label lblUserName;

    private final PageParameters parameters;

    private String userAccountKey;

    private SamlAuthInfo samlAuthInfo;

    public SamlSignControlPanel(String id, PageParameters parameters) {
        super(id);

        this.parameters = parameters;

        this.signControlPanel = new Form("signControlPanel");
        this.add(this.signControlPanel);

        this.logoutButton = new Button("logoutButton", Model.of("ログアウト")) {
            @Override
            public void onSubmit() {
                this.setResponsePage(SamlSLOPage.class, SamlSignControlPanel.this.parameters);
            }
        };
        this.signControlPanel.add(this.logoutButton);

        this.lblUserName = new Label("userName", Model.of("未ログイン"));
        this.signControlPanel.add(this.lblUserName);

        this.loginButton = new Button("loginButton", Model.of("ログイン")) {
            @Override
            public void onSubmit() {
                this.setResponsePage(SamlSSOPage.class, SamlSignControlPanel.this.parameters);
            }
        };
        this.signControlPanel.add(this.loginButton);

    }

    public void setUserAcclountKey(String key) {
        this.userAccountKey = key;
    }

    public void showStatus(WebPage parent) {
        SamlSession session = (SamlSession) parent.getSession();
        SamlProcess sprocess = new SamlProcess(parent, SamlProcess.MODE_CHECKLOGIN);
        if (sprocess.getStatus() == SamlAuthInfo.STATUS_AUTHENTICATED) {
            this.samlAuthInfo = sprocess.getAuthInfo();

            session.setSamlAuthInfo(this.samlAuthInfo);
            Roles roles = session.getRoles();
            /*
            Logger.getLogger(this.getClass().getName()).log(Level.INFO,
                    roles.toString()
            );*/

            String username = this.samlAuthInfo.getAttributeString(this.userAccountKey);
            if (username.isEmpty()) {
                username = "WARN: '" + this.userAccountKey + "' is not supplied.";
            }
            this.lblUserName.setDefaultModelObject(username);

            this.loginButton.setVisible(false);
        } else {
            this.samlAuthInfo = null;
            this.logoutButton.setVisible(false);
        }
    }

    public SamlAuthInfo getAuth() {
        return this.samlAuthInfo;
    }

}
