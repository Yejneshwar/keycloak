/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.forms.login.freemarker.model;

import org.jboss.logging.Logger;
import org.keycloak.authentication.requiredactions.util.UpdateProfileContext;
import org.keycloak.util.regionmapper.DataMap;
import org.keycloak.util.regionmapper.RegionToFlagMapper;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ProfileBean {

    private static final Logger logger = Logger.getLogger(ProfileBean.class);

    private UpdateProfileContext user;
    private MultivaluedMap<String, String> formData;
    private List<LocaleDataMap> localeData;
    private String selectedLocale;

    private final Map<String, String> attributes = new HashMap<>();

    public ProfileBean(UpdateProfileContext user, MultivaluedMap<String, String> formData) {
        this.selectedLocale = selectedLocale;
        this.user = user;
        this.formData = formData;
        this.localeData = RegionToFlagMapper.getInstance().getList()
        .map(LocaleDataMap::new)
        .collect(Collectors.toList());

        Map<String, List<String>> modelAttrs = user.getAttributes();
        if (modelAttrs != null) {
            for (Map.Entry<String, List<String>> attr : modelAttrs.entrySet()) {
                List<String> attrValue = attr.getValue();
                if (attrValue != null && attrValue.size() > 0) {
                    attributes.put(attr.getKey(), attrValue.get(0));
                }

                if (attrValue != null && attrValue.size() > 1) {
                    logger.warnf("There are more values for attribute '%s' of user '%s' . Will display just first value", attr.getKey(), user.getUsername());
                }
            }
        }
        if (formData != null) {
            for (String key : formData.keySet()) {
                if (key.startsWith("user.attributes.")) {
                    String attribute = key.substring("user.attributes.".length());
                    attributes.put(attribute, formData.getFirst(key));
                }
            }
        }

    }

    public String getSelectedLocale(){
        return selectedLocale;
    }

    public List<LocaleDataMap> getLocaleData(){
        return localeData;
    }

    public boolean isEditUsernameAllowed() {
        return user.isEditUsernameAllowed();
    }

    public boolean isEditEmailAllowed() {
        return user.isEditEmailAllowed();
    }

    public String getUsername() { return formData != null ? formData.getFirst("username") : user.getUsername(); }

    public String getFirstName() {
        return formData != null ? formData.getFirst("firstName") : user.getFirstName();
    }

    public String getLastName() {
        return formData != null ? formData.getFirst("lastName") : user.getLastName();
    }

    public String getPhoneNumberLocale() {
        return formData != null ? formData.getFirst("phoneNumberLocale") : user.getPhoneNumberLocale();
    }

    public String getPhoneNumber() {
        return formData != null ? formData.getFirst("phoneNumber") : user.getPhoneNumber();
    }

    public String getEmail() {
        return formData != null ? formData.getFirst("email") : user.getEmail();
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public static class LocaleDataMap {

        private final String name;
        private final String ISOName;
        private final String callingCode;
        private final String flag;

        public LocaleDataMap(DataMap data) {
            // System.out.println("Name : " + data.getName());
            // System.out.println("ISO : " + data.getISOName());
            // System.out.println("calling : " + data.getCallingCode());
            
            this.name = data.getName();
            this.ISOName = data.getISOName();
            this.callingCode = data.getCallingCode();
            this.flag = data.getFlag();
        }

        public String getName() {
            return name;
        }

        public String getISOName() {
            return ISOName;
        }
        public String getCallingCode() {
            return callingCode;
        }
        public String getFlag() {
            return flag;
        }
    }
}
