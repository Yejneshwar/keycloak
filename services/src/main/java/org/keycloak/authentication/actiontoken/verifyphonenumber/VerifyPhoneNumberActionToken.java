/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.authentication.actiontoken.verifyphonenumber;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.keycloak.authentication.actiontoken.DefaultActionToken;

/**
 * Representation of a token that represents a time-limited verify e-mail action.
 *
 * @author Yejneshwar Sivamoorthy
 */
public class VerifyPhoneNumberActionToken extends DefaultActionToken {

    public static final String TOKEN_TYPE = "verify-phone-number";

    private static final String JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID = "oasid";

    @JsonProperty(value = JSON_FIELD_ORIGINAL_AUTHENTICATION_SESSION_ID)
    private String originalAuthenticationSessionId;

    public VerifyPhoneNumberActionToken(String userId, int absoluteExpirationInSecs, String compoundAuthenticationSessionId, String phoneNumber, String clientId) {
        super(userId, TOKEN_TYPE, absoluteExpirationInSecs, null, compoundAuthenticationSessionId);
        setPhoneNumber(phoneNumber);
        this.issuedFor = clientId;
    }

    private VerifyPhoneNumberActionToken() {
    }

    public String getCompoundOriginalAuthenticationSessionId() {
        return originalAuthenticationSessionId;
    }

    public void setCompoundOriginalAuthenticationSessionId(String originalAuthenticationSessionId) {
        this.originalAuthenticationSessionId = originalAuthenticationSessionId;
    }
}
