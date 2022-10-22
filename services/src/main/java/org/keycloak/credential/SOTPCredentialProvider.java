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
package org.keycloak.credential;

import org.jboss.logging.Logger;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.dto.OTPCredentialData;
import org.keycloak.models.credential.dto.OTPSecretData;
import org.keycloak.models.utils.HmacOTP;
import org.keycloak.models.utils.TimeBasedOTP;
import org.keycloak.models.utils.SmsOTP;
import org.keycloak.models.utils.EmailBasedOTP;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:yejneshwar@gmail.com">Yejneshwar Sivaoorthy</a>
 * @version $Revision: 1 $
 */
public class SOTPCredentialProvider implements CredentialProvider<OTPCredentialModel>, CredentialInputValidator/*, OnUserCache*/ {
    
    private static final Logger logger = Logger.getLogger(SOTPCredentialProvider.class);

    protected KeycloakSession session;

    public SOTPCredentialProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user, OTPCredentialModel credentialModel) {
        if (credentialModel.getCreatedDate() == null) {
            credentialModel.setCreatedDate(Time.currentTimeMillis());
        }
        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public OTPCredentialModel getCredentialFromModel(CredentialModel model) {
        return OTPCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        System.out.println("get Type : at sotp Provider -- " + getType() + " Supports? : " + getType().equals(credentialType) + " GET = " + getType() + " credType = " + credentialType  );
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        System.out.println("Supports Cred type : " + credentialType + " " + supportsCredentialType(credentialType) );
        if (!supportsCredentialType(credentialType)) return false;
        boolean credentialPresent =  user.credentialManager().getStoredCredentialsByTypeStream(credentialType).findAny().isPresent();
        boolean phoneNumberValid = (user.getPhoneNumber() != null && !ObjectUtil.isBlank(user.getPhoneNumber()));
        boolean phoneNumberLocaleValid = (user.getPhoneNumberLocale() != null && !ObjectUtil.isBlank(user.getPhoneNumberLocale()) && user.getPhoneNumberLocale().length() == 2);
        boolean phoneNumberVerified = user.isPhoneNumberVerified();
        System.out.printf("4 User attribte checks : --\nCredential present? : %s\nNumber valid? : %s\nLocale valid? : %s\nNumber verified? : %s\n",(credentialPresent),(phoneNumberValid),(phoneNumberLocaleValid),(phoneNumberVerified));
        return (credentialPresent && phoneNumberValid && phoneNumberLocaleValid && phoneNumberVerified);
    }

    public boolean isConfiguredFor(RealmModel realm, UserModel user){
        return isConfiguredFor(realm, user, getType());
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!(credentialInput instanceof UserCredentialModel)) {
            logger.debug("Expected instance of UserCredentialModel for CredentialInput");
            return false;

        }
        String challengeResponse = credentialInput.getChallengeResponse();
        if (challengeResponse == null) {
            return false;
        }
        if (ObjectUtil.isBlank(credentialInput.getCredentialId())) {
            logger.debugf("CredentialId is null when validating credential of user %s", user.getUsername());
            return false;
        }

        CredentialModel credential = user.credentialManager().getStoredCredentialById(credentialInput.getCredentialId());
        OTPCredentialModel otpCredentialModel = OTPCredentialModel.createFromCredentialModel(credential);
        OTPSecretData secretData = otpCredentialModel.getOTPSecretData();
        OTPCredentialData credentialData = otpCredentialModel.getOTPCredentialData();
        OTPPolicy policySOTP = realm.getSmsOTPPolicy();

        if (OTPCredentialModel.SOTP.equals(credentialData.getSubType())){
            SmsOTP validator = new SmsOTP(credentialData.getAlgorithm(), credentialData.getDigits(), credentialData.getPeriod(), policySOTP.getLookAheadWindow());
            return validator.validateSOTP(challengeResponse,session.getContext().getAuthenticationSession());
        }
        return false;
    }

    @Override
    public String getType() {
        return OTPCredentialModel.SOTP;
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        return CredentialTypeMetadata.builder()
                .type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR)
                .displayName("Sotp-display-name")
                .helpText("Sotp-help-text")
                .iconCssClass("kcAuthenticatorSOTPClass")
                .createAction(UserModel.RequiredAction.UPDATE_PHONE_NUMBER.toString())
                .removeable(true)
                .build(session);
    }
}
