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

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.Error;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.logout.LogoutRequestParams;
import com.onelogin.saml2.util.Util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestParameters;
import org.tiny.wicket.SamlMainPage;
import org.w3c.dom.Document;

/**
 *
 * @author DtmOyaji
 */
public class SamlProcess {

    public static final int MODE_LOGIN = 0;

    public static final int MODE_CHECKLOGIN = 1;

    public static final int MODE_LOGOUT = 2;

    public static final String KEY_SAML_RESPONSE = "SAMLResponse";

    public static final String DEFAULT_CODE = "UTF-8";

    private int loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

    private IRequestParameters params;

    private WebPage page;

    private Auth auth;

    private SamlAuthInfo samlAuthInfo;

    private String settingFile = null;

    /**
     * 初期化。プロパティファイル名は規定値(onelogin.saml.sample.properties)
     * @param request
     * @param response
     * @param mode 
     */
    public SamlProcess(HttpServletRequest request, HttpServletResponse response, int mode) {
        resolve(request, response, mode);
    }

    /**
     * 初期化。プロパティファイルを指定する。
     * @param settingFilePath
     * @param request
     * @param response
     * @param mode 
     */
    public SamlProcess(String settingFilePath, HttpServletRequest request, HttpServletResponse response, int mode) {
        this.settingFile = settingFilePath;
        resolve(request, response, mode);
    }
    
    /**
     * 初期化。WebPageよりリクエストとレスポンスを抽出して動作する。プロパティファイルは規定値。
     * @param page
     * @param mode 
     */
    public SamlProcess(WebPage page, int mode) {
        this.resolve(page, mode);
    }

    /**
     * 初期化。WebPageよりリクエストとレスポンスを抽出して動作する。プロパティファイル名を指定する。
     * @param settingFilePath
     * @param page
     * @param mode 
     */
    public SamlProcess(String settingFilePath, WebPage page, int mode) {
        this.settingFile = settingFilePath;
        this.resolve(page, mode);
    }

    private void resolve(WebPage page, int mode) {
        this.page = page;
        HttpServletRequest request = (HttpServletRequest) page.getRequest().getContainerRequest();
        HttpServletResponse response = (HttpServletResponse) page.getResponse().getContainerResponse();
        this.params = this.page.getRequest().getRequestParameters();
        resolve(request, response, mode);
    }

    private Auth buildAuth(HttpServletRequest request, HttpServletResponse response) {
        Auth rvalue = null;
        try {
            if (this.settingFile == null) {
                rvalue = new Auth(request, response);
            } else {
                rvalue = new Auth(this.settingFile, request, response);
            }
        } catch (IOException | SettingsException | Error ex) {
            Logger.getLogger(SamlProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rvalue;
    }

    /**
     * ログイン、ログインチェック、ログアウトの振り分け処理
     * @param request
     * @param response
     * @param mode 
     */
    private void resolve(HttpServletRequest request, HttpServletResponse response, int mode) {
        
        this.auth = this.buildAuth(request, response);
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
        
    }

    /**
     * ログアウト実行
     */
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

    /**
     * ログイン状態の確認。
     * 結果はgetStatusで取得する。
     */
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

        String param = this.params.getParameterValue(KEY_SAML_RESPONSE).toString();
        if (param != null) {
            try {
                String samlResponseString = new String(Util.base64decoder(param), DEFAULT_CODE);
                Document samlResponseDocument = Util.loadXML(samlResponseString); // エラー発生個所
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

    /**
     * ログインの実行
     * 実行結果はgetStatusで取得する。
     */
    private void doLogin() {
        this.loginStatus = SamlAuthInfo.STATUS_NOTAUTHENTICATED;

        try {
            this.checkLogin();
            if (this.loginStatus == SamlAuthInfo.STATUS_NOTAUTHENTICATED) {
                this.auth.login();
                this.loginStatus = SamlAuthInfo.STATUS_AUTHENTICATED;
            }
        } catch (IOException | SettingsException ex) {
            Logger.getLogger(SamlMainPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ログイン状態の取得
     * @return 
     */
    public int getStatus() {
        return this.loginStatus;
    }

    /**
     * ユーザーの認証情報を取得する。
     * @return SamlAuthInfo
     */
    public SamlAuthInfo getAuthInfo() {
        return this.samlAuthInfo;
    }

    /**
     * oneLoginのAuthを取得する。
     * @return 
     * @deprecated 完全にラップするので非推奨とした。
     */
    @Deprecated
    public Auth getAuth() {
        return this.auth;
    }

}
