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

    private Form f;

    private Button logoutButton;

    private PageParameters parameters;

    public SamlSignControlPanel(String id, PageParameters parameters) {
        super(id);

        this.parameters = parameters;

        this.f = new Form("logoutForm");
        this.add(this.f);

        this.logoutButton = new Button("logoutButton", Model.of("ログアウト")) {

            @Override
            public void onSubmit() {
                logoutProcess();
            }
        };
        this.f.add(this.logoutButton);
    }

    public void logoutProcess() {
        this.setResponsePage(SamlLogoutPage.class, this.parameters);
    }

}
