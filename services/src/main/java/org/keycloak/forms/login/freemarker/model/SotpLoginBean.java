package org.keycloak.forms.login.freemarker.model;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.keycloak.authentication.authenticators.browser.OTPFormAuthenticator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.OTPCredentialProvider;
import org.keycloak.credential.OTPCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.Constants;

import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.common.util.Time;


/**
 * Used for SOTP login
 *
 * @author <a href="mailto:yejneshwar@gmail.com">Yejneshwar Sivamoorthy</a>
 */
public class SotpLoginBean {

    private final String selectedCredentialId;
    private final List<OTPCredential> userOtpCredentials;
    private final String update;
    private final String number;
    private final String locale;
    private final String time;

    public SotpLoginBean(KeycloakSession session, RealmModel realm, UserModel user, String selectedCredentialId) {
        this.update = session.getContext().getUri().getQueryParameters().getFirst("execution");
        if ("UPDATE_PHONE_NUMBER".equals(this.update)){
            System.out.println("UPDATING PHONE NUMBER!!!!!!!!");
            this.userOtpCredentials = new ArrayList<OTPCredential>();
            this.selectedCredentialId = null;
            this.number = session.getContext().getAuthenticationSession().getAuthNote(Constants.VERIFY_PHONE_NUMBER_UPDATE_KEY);
            this.locale = session.getContext().getAuthenticationSession().getAuthNote(Constants.VERIFY_PHONE_NUMBER_LOCALE_UPDATE_KEY);
            this.time = session.getContext().getAuthenticationSession().getAuthNote(Constants.VERIFY_PHONE_NUMBER_TIME_SET_KEY);
        }
        else{
        this.number = null;
        this.locale = null;
        this.time = session.getContext().getAuthenticationSession().getAuthNote(Constants.VERIFY_PHONE_NUMBER_TIME_SET_KEY);
        this.userOtpCredentials = session.userCredentialManager().getStoredCredentialsByTypeStream(realm, user, OTPCredentialModel.TYPE)
                .map(OTPCredential::new)
                .collect(Collectors.toList());
        System.out.println(this.userOtpCredentials);

        // This means user did not yet manually selected any OTP credential through the UI. So just go with the default one with biggest priority
        if (selectedCredentialId == null || selectedCredentialId.isEmpty()) {
            OTPCredentialProvider otpCredentialProvider = (OTPCredentialProvider)session.getProvider(CredentialProvider.class, OTPCredentialProviderFactory.PROVIDER_ID);
            OTPCredentialModel otpCredential = otpCredentialProvider
                    .getDefaultCredential(session, realm, user);

            selectedCredentialId = otpCredential==null ? null : otpCredential.getId();
        }

        this.selectedCredentialId = selectedCredentialId;

        }
        
    }


    public List<OTPCredential> getUserOtpCredentials() {
        return userOtpCredentials;
    }

    public String getSelectedCredentialId() {
        return selectedCredentialId;
    }

    public String getUpdate() {
        return update;
    }

    public String getNumber() {
        return number;
    }
    public String getLocale() {
        return locale;
    }
    public String getTime() {
        return time;
    }


    public static class OTPCredential {

        private final String id;
        private final String userLabel;
        private final String secret;
        private final String data;
        private final String type;

        public OTPCredential(CredentialModel credentialModel) {
            System.out.println("Secret : " + credentialModel.getSecretData());
            System.out.println("Data : " + credentialModel.getCredentialData());
            System.out.println("Type : " + credentialModel.getType());
            
            this.id = credentialModel.getId();
            // TODO: "Unnamed" OTP credentials should be displayed in the UI in gray
            this.userLabel = credentialModel.getUserLabel() == null || credentialModel.getUserLabel().isEmpty() ? OTPFormAuthenticator.UNNAMED : credentialModel.getUserLabel();
            // if (credentialModel.getOTPCredentialData().getSubType().equals(OTPCredentialModel.SOTP)) this.userLabel = credentialModel.getOTPSecretData().getValue();

            this.secret = credentialModel.getSecretData();
            this.data = credentialModel.getCredentialData();
            this.type = credentialModel.getType();
        }

        public String getId() {
            return id;
        }

        public String getUserLabel() {
            return userLabel;
        }
        public String getSecretData() {
            return secret;
        }
        public String getCredentialData() {
            return data;
        }
        public String getType() {
            return type;
        }
    }
}
