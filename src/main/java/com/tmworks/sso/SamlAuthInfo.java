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

    private String uncupsul(ArrayList<String> value) {
        if (value.isEmpty()) {
            return "";
        }
        String rvalue = value.get(0);
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
        if (this.attributes == null) {
            this.attributes = new LinkedHashMap<>();
            this.unpackAttributes();
        }
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

    /**
     * user attributes covert from Map to String.
     *
     * @return
     */
    private String packAttributes() {
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

    /**
     * user attributes convert from String to Map.
     *
     * @param xvalue
     */
    private void unpackAttributes() {
        String xvalue = this.uncupsul(this.get("SAMLAttributes"));
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
}
