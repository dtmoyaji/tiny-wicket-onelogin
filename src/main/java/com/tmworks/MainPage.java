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
package com.tmworks;

import com.onelogin.saml2.Auth;
import com.tmworks.sso.SamlLoginPage;
import com.tmworks.sso.SamlProcess;
import com.tmworks.sso.SamlSignControlPanel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author bythe
 */
public class MainPage extends WebPage {

    private Button button;

    private SamlSignControlPanel signControlPanel;

    private Label log;

    public MainPage(final PageParameters parameters) {
        super(parameters);

        SamlProcess checkLogin = new SamlProcess(
                (HttpServletRequest) this.getRequest().getContainerRequest(),
                (HttpServletResponse) this.getResponse().getContainerResponse(),
                SamlProcess.MODE_CHECKLOGIN
        );

        Auth auth = checkLogin.getAuth();

        PageParameters responseParameter = new PageParameters();
        if (auth != null) {
            responseParameter.add("id", (auth.getLastAssertionId()) == null ? "" : auth.getLastAssertionId());
            responseParameter.add("name_id", (auth.getNameId() == null) ? "" : auth.getNameId());
            responseParameter.add("session_index", (auth.getSessionIndex() == null) ? "" : auth.getSessionIndex());
            responseParameter.add("nameid_format", (auth.getSessionIndex() == null) ? "" : auth.getNameIdFormat());
        }

        this.signControlPanel = new SamlSignControlPanel(
                "signControlPanel",
                responseParameter
        );

        this.add(this.signControlPanel);
        this.log = (Label) new Label("log", Model.of("")).setEscapeModelStrings(false);
        this.add(log);

        if (checkLogin.getStatus() == SamlProcess.STATUS_AUTHENTICATED) {
            try {
                auth.processResponse();
            } catch (Exception ex) {
                Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (auth.isAuthenticated()) {
            String data = "";
            List<String> names = auth.getAttributesName();
            data = names.stream().map((name) -> name + " : " + auth.getAttribute(name) + "<br>").reduce(data, String::concat);
            this.log.setDefaultModelObject(data);
        } else {
            this.setResponsePage(SamlLoginPage.class);
        }
    }

}
