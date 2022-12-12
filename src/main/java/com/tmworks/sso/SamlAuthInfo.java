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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;

/**
 *
 * @author bythe
 */
public class SamlAuthInfo extends LinkedHashMap<String, ArrayList<String>> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_NOTAUTHENTICATED = 0;

    public static final int STATUS_AUTHENTICATED = 1;

    public static final String COOKIE_SESSIONTINDEX = "-sessionIndex";
    public static final String COOKIE_NAMEID = "-nameId";
    public static final String COOKIE_PARAMETERS = "-parameters";

    public static final String COOKIE_KEY_ROLE = "role";
    public static final String COOKIE_ITEM_EQUALS = "/";
    public static final String COOKIE_ITEM_END = ":";
    public static final String COOKIE_ITEM_INNER_SEPARATOR = "#";

    private String sessionIndex;
    private String nameId;

    private Map<String, List<String>> attributes;

    public SamlAuthInfo(Auth auth) {
        this.sessionIndex = auth.getSessionIndex();
        this.nameId = auth.getNameId();
        this.attributes = auth.getAttributes();
        this.put("SAMLNameId", this.cupsul(nameId));
        this.put("SAMLSessinIndex", this.cupsul(sessionIndex));
        this.put("SAMLAttributes",
                this.cupsul(this.packAttributes()));
    }

    private ArrayList<String> cupsul(String value) {
        ArrayList<String> rvalue = new ArrayList<>();
        rvalue.add(value);
        return rvalue;
    }

    public SamlAuthInfo() {
        this.sessionIndex = "";
        this.nameId = "";
        this.attributes = null;
        this.put("SAMLNameId", this.cupsul(nameId));
        this.put("SAMLSessinIndex", this.cupsul(sessionIndex));
        this.put("SAMLAttributes",
                this.cupsul(this.packAttributes()));
    }

    public List<String> getAttribute(String key) {
        return this.attributes.get(key);
    }

    public void setAttributes(Map<String, List<String>> attrs) {
        this.attributes = attrs;
    }

    public Map<String, List<String>> getAttributes() {
        return this.attributes;
    }

    public void setSessionIndex(String index) {
        this.sessionIndex = index;
    }

    public String getSessionIndex() {
        if (this.sessionIndex == null) {
            this.sessionIndex = "";
        }
        return this.sessionIndex;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getNameId() {
        if (this.nameId == null) {
            this.nameId = "";
        }
        return this.nameId;
    }

    public void saveCookie(WebResponse webResponse, String cookiePrefix, int maxTime) {
        webResponse.addCookie(
                createCookie(cookiePrefix + COOKIE_SESSIONTINDEX, this.getSessionIndex(), maxTime)
        );
        webResponse.addCookie(
                createCookie(cookiePrefix + COOKIE_NAMEID, this.getNameId(), maxTime)
        );
        String paramv = this.packAttributes();
        webResponse.addCookie(
                createCookie(cookiePrefix + COOKIE_PARAMETERS, paramv, maxTime)
        );
    }

    public String packAttributes() {
        String paramv = "";
        if (this.attributes != null) {
            Set<String> keys = this.attributes.keySet();
            for (String key : keys) {
                List<String> value = this.attributes.get(key);
                if (!value.isEmpty()) {
                    if (key.toLowerCase().equals(SamlAuthInfo.COOKIE_KEY_ROLE)) {
                        String rolelist = "";
                        for (String item : value) {
                            rolelist += COOKIE_ITEM_INNER_SEPARATOR + item;
                        }
                        if (!rolelist.isEmpty()) {
                            rolelist = rolelist.substring(1);
                        }
                        paramv += key + COOKIE_ITEM_EQUALS + rolelist + COOKIE_ITEM_END;
                    } else {
                        paramv += key + COOKIE_ITEM_EQUALS + value.get(0) + COOKIE_ITEM_END;
                    }
                }
            }
        }
        return paramv;
    }

    public static Cookie createCookie(String name, String value, int MaxTime) {
        Cookie rvalue = new Cookie(name, value);
        rvalue.setMaxAge(MaxTime);
        return rvalue;
    }

    public void unpackAttributes(String xvalue) {
        if (!xvalue.isEmpty()) {
            String[] paramsrc = xvalue.split(SamlAuthInfo.COOKIE_ITEM_END);
            for (String param : paramsrc) {
                String[] keyvalue = param.split(SamlAuthInfo.COOKIE_ITEM_EQUALS);
                String key = keyvalue[0];
                String value = keyvalue[1];
                if (key.toLowerCase().equals(SamlAuthInfo.COOKIE_KEY_ROLE)) {
                    String[] values = value.split(SamlAuthInfo.COOKIE_ITEM_INNER_SEPARATOR);
                    ArrayList<String> roleList = new ArrayList<>();
                    roleList.addAll(Arrays.asList(values));
                    this.attributes.put(key, roleList);
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(value);
                    this.attributes.put(key, list);
                }
            }
        }
    }

    public boolean loadCookie(WebRequest webRequest, String cookiePrefix) {
        try {

            this.sessionIndex = this.getCookieValue(webRequest, cookiePrefix + COOKIE_SESSIONTINDEX);
            this.nameId = this.getCookieValue(webRequest, cookiePrefix + COOKIE_NAMEID);
            String params = this.getCookieValue(webRequest, cookiePrefix + COOKIE_PARAMETERS);
            if (!params.isEmpty()) {
                String[] paramsrc = params.split(SamlAuthInfo.COOKIE_ITEM_END);
                for (String param : paramsrc) {
                    String[] keyvalue = param.split(SamlAuthInfo.COOKIE_ITEM_EQUALS);
                    String key = keyvalue[0];
                    String value = keyvalue[1];
                    if (key.toLowerCase().equals(SamlAuthInfo.COOKIE_KEY_ROLE)) {
                        String[] values = value.split(SamlAuthInfo.COOKIE_ITEM_INNER_SEPARATOR);
                        ArrayList<String> roleList = new ArrayList<>();
                        roleList.addAll(Arrays.asList(values));
                        this.attributes.put(key, roleList);
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(value);
                        this.attributes.put(key, list);
                    }
                }
            }

        } catch (NullPointerException ex) {
            Logger.getLogger(AuthenticatedSession.class.getName()).log(Level.SEVERE, null, ex);
        }
        return !this.nameId.isEmpty();
    }

    private String getCookieValue(WebRequest webRequest, String cookieKey) {
        String rvalue = "";
        Cookie cookie = webRequest.getCookie(cookieKey);
        if (cookie != null) {
            rvalue = cookie.getValue();
        }
        return rvalue;
    }

    public static void clearCookie(WebRequest webRequest, WebResponse webResponse, String cookiePrefix) {
        webResponse.addCookie(SamlAuthInfo.createCookie(cookiePrefix + COOKIE_NAMEID, "", 0));
        webResponse.addCookie(SamlAuthInfo.createCookie(cookiePrefix + COOKIE_SESSIONTINDEX, "", 0));
        webResponse.addCookie(SamlAuthInfo.createCookie(cookiePrefix + COOKIE_PARAMETERS, "", 0));
        webResponse.disableCaching();
    }

}
