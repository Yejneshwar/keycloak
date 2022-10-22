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

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.HmacOTP;

import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


import org.keycloak.events.Details;

/**
 * Used for UpdateSotp required action
 *
 * @author <a href="mailto:yejneshwar@gmail.com">Yejneshwar Sivamoorthy</a>
 */
public class SotpBean {

    private final RealmModel realm;
    private final String sotpSecret;
    private final boolean enabled;
    private UriBuilder uriBuilder;
    private final List<CredentialModel> otpCredentials;
    private final String attempts;
    private final String lastSendTime;
    private final String expireTime;


    public SotpBean(KeycloakSession session,RealmModel realm, UserModel user, UriBuilder uriBuilder) {
        System.out.println("Running SotpBean!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.realm = realm;
        this.uriBuilder = uriBuilder;
        this.enabled = user.credentialManager().isConfiguredFor(OTPCredentialModel.SOTP);
        if (enabled) {
            otpCredentials = user.credentialManager().getStoredCredentialsByTypeStream(OTPCredentialModel.SOTP)
                    .collect(Collectors.toList());
        } else { 
            otpCredentials = Collections.EMPTY_LIST;
        }
        this.sotpSecret = HmacOTP.generateSecret(20);

        this.attempts = session.getContext().getAuthenticationSession().getAuthNote(Details.ATTEMPTS_REMAINING);
        this.lastSendTime = session.getContext().getAuthenticationSession().getAuthNote(Details.LAST_OTP_SEND);
        this.expireTime = session.getContext().getAuthenticationSession().getAuthNote(Details.OTP_EXPIRY);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getSotpSecret() {
        return sotpSecret;
    }

    public OTPPolicy getPolicy() {
        return realm.getSmsOTPPolicy();
    }

    public List<CredentialModel> getOtpCredentials() {
        return otpCredentials;
    }

    public String getAttempts(){
        return attempts;
    }

    public String getLastSendTime(){
        return lastSendTime;
    }

    public String getExpireTime(){
        return expireTime;
    }

}
