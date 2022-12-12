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

import com.tmworks.sso.SamlSignControlPanel;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 *
 * @author bythe
 */
//@AuthorizeInstantiation("user")
public class MainPage extends WebPage {

    private Button button;

    private SamlSignControlPanel signControlPanel;

    private Label log;

    public MainPage(final PageParameters parameters) {
        super(parameters);
        
        Session session = this.getSession();

        this.signControlPanel = new SamlSignControlPanel(
                "signControlPanel",
                parameters
        );
        this.signControlPanel.showStatus(this);
        this.add(this.signControlPanel);

        this.log = (Label) new Label("log", Model.of("")).setEscapeModelStrings(false);
        this.add(log);
        
    }
}
