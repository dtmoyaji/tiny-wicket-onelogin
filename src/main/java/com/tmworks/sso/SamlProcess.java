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
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.logout.LogoutRequestParams;
import com.onelogin.saml2.util.Util;
import com.tmworks.MainPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.w3c.dom.Document;

/**
 *
 * @author bythe
 */
public class SamlProcess {

    public static final int MODE_LOGIN = 0;

    public static final int MODE_CHECKLOGIN = 1;

    public static final int MODE_LOGOUT = 2;

    public static final int STATUS_NOTAUTHENTICATED = 0;

    public static final int STATUS_AUTHENTICATED = 1;

    private int loginStatus = STATUS_NOTAUTHENTICATED;

    private PageParameters params;

    private WebPage page;

    private Auth auth;

    private Session session;

    public SamlProcess(HttpServletRequest request, HttpServletResponse response, int mode) {
        resolve(request, response, mode);
    }

    public SamlProcess(WebPage page, int mode) {
        this.page = page;
        HttpServletRequest request = (HttpServletRequest) page.getRequest().getContainerRequest();
        HttpServletResponse response = (HttpServletResponse) page.getResponse().getContainerResponse();
        resolve(request, response, mode);
    }

    public SamlProcess(WebPage page, PageParameters parameters, int mode) {
        this.page = page;
        this.params = parameters;
        HttpServletRequest request = (HttpServletRequest) page.getRequest().getContainerRequest();
        HttpServletResponse response = (HttpServletResponse) page.getResponse().getContainerResponse();
        resolve(request, response, mode);
    }

    private void resolve(HttpServletRequest request, HttpServletResponse response, int mode) {
        try {
            this.auth = new Auth(request, response);

            switch (mode) {
                case MODE_LOGIN:
                    this.doLogin(request, response);
                    break;
                case MODE_CHECKLOGIN:
                    this.checkLogin(request, response);
                    break;
                case MODE_LOGOUT:
                    this.doLogout(request, response);
                    break;
            }
        } catch (IOException | SettingsException | Error ex) {
            Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    private void doLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            LogoutRequestParams lparams = new LogoutRequestParams(
                    request.getParameter("session_index"),
                    request.getParameter("name_id")
            );

            auth.logout(null, lparams, false);
            System.out.println(this.auth.isAuthenticated());
        } catch (IOException | SettingsException ex) {
            Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void checkLogin(HttpServletRequest request, HttpServletResponse response) {
        this.loginStatus = SamlProcess.STATUS_NOTAUTHENTICATED;

        String param = request.getParameter("SAMLResponse");
        if (param != null) {
            try {
                String samlResponseString = new String(Util.base64decoder(param), "UTF-8");
                Document samlResponseDocument = Util.loadXML(samlResponseString);
                if (samlResponseDocument != null) {
                    this.loginStatus = SamlProcess.STATUS_AUTHENTICATED;
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response) {
        this.loginStatus = SamlProcess.STATUS_NOTAUTHENTICATED;
        try {
            if (request.getParameter("SAMLResponse") == null) {
                auth.login();
            } else {
                auth.processResponse();
                if (!auth.isAuthenticated()) {
                    auth.getErrors().forEach((reason) -> {
                        System.out.println(reason);
                    });
                } else {
                    this.loginStatus = SamlProcess.STATUS_AUTHENTICATED;
                }
            }
        } catch (IOException | SettingsException | com.onelogin.saml2.exception.Error ex) {
            Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MainPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getStatus() {
        return this.loginStatus;
    }

    public Auth getAuth() {
        return this.auth;
    }
}
