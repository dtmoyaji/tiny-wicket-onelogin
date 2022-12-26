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
package com.tmworks.sso;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.logout.LogoutRequestParams;
import com.onelogin.saml2.util.Util;
import com.tmworks.SamlMainPage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestParameters;
import org.w3c.dom.Document;

/**
 *
 * @author DtmOyaji
 */
public class SamlProcess {

    public static final int MODE_LOGIN = 0;

    public static final int MODE_CHECKLOGIN = 1;

    public static final int MODE_LOGOUT = 2;

    private int loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

    private IRequestParameters params;

    private WebPage page;

    private Auth auth;

    private SamlAuthInfo samlAuthInfo;

    public SamlProcess(HttpServletRequest request, HttpServletResponse response, int mode) {
        resolve(request, response, mode);
    }

    public SamlProcess(WebPage page, int mode) {
        this.resolve(page, mode);
    }

    private void resolve(WebPage page, int mode) {
        this.page = page;
        HttpServletRequest request = (HttpServletRequest) page.getRequest().getContainerRequest();
        HttpServletResponse response = (HttpServletResponse) page.getResponse().getContainerResponse();
        this.params = this.page.getRequest().getRequestParameters();
        resolve(request, response, mode);
    }

    private void resolve(HttpServletRequest request, HttpServletResponse response, int mode) {
        try {
            this.auth = new Auth(request, response);
            this.samlAuthInfo = new SamlAuthInfo(this.auth);

            switch (mode) {
                case MODE_LOGIN:
                    this.doLogin();
                    break;
                case MODE_CHECKLOGIN:
                    this.checkLogin();
                    break;
                case MODE_LOGOUT:
                    this.doLogout();
                    break;
            }
        } catch (IOException | SettingsException | Error ex) {
            Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doLogout() {
        try {

            SamlSession asession = (SamlSession) this.page.getSession();

            LogoutRequestParams lparams = new LogoutRequestParams(
                    asession.getSamlAuthInfo().getSessionIndex(),
                    asession.getSamlAuthInfo().getNameId()
            );

            asession.setSamlAuthInfo(new SamlAuthInfo());
            this.loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

            auth.logout(null, lparams, false);

            System.out.println(this.auth.isAuthenticated());
        } catch (IOException | SettingsException ex) {
            Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void checkLogin() {
        this.loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

        SamlSession asession = (SamlSession) this.page.getSession();
        SamlAuthInfo sinfo = asession.getSamlAuthInfo();
        if (sinfo != null) {
            if (!sinfo.getSessionIndex().isEmpty()) {
                this.samlAuthInfo = sinfo;
                this.loginStatus = SamlAuthInfo.STATUS_AUTHENTICATED;
                return;
            }
        }

        String param = this.params.getParameterValue("SAMLResponse").toString();
        if (param != null) {
            try {
                String samlResponseString = new String(Util.base64decoder(param), "UTF-8");
                Document samlResponseDocument = Util.loadXML(samlResponseString);
                if (samlResponseDocument != null) {
                    this.auth.processResponse();
                    if (this.auth.isAuthenticated()) {
                        this.samlAuthInfo = new SamlAuthInfo(this.auth);
                        this.loginStatus = SamlAuthInfo.STATUS_AUTHENTICATED;
                    } else {
                        Logger.getLogger(this.getClass().getName()).log(Level.INFO, this.auth.getErrors().toString());
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void doLogin() {
        this.loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

        try {
            this.checkLogin();
            if (this.loginStatus == SamlAuthInfo.STATUS_NOTAUTHENTICATED) {
                auth.login();
                this.loginStatus = SamlAuthInfo.STATUS_AUTHENTICATED;
            }
        } catch (IOException | SettingsException ex) {
            Logger.getLogger(SamlMainPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getStatus() {
        return this.loginStatus;
    }

    public SamlAuthInfo getAuthInfo() {
        return this.samlAuthInfo;
    }

    public Auth getAuth() {
        return this.auth;
    }

}
