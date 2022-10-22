/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authentication.requiredactions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.InitiatedActionSupport;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.actiontoken.updatephonenumber.UpdatePhoneNumberActionToken;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.sms.SMSException;
import org.keycloak.sms.SMSTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.Templates;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.services.Urls;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.userprofile.EventAuditingAttributeChangeListener;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;
import org.keycloak.services.managers.AuthenticationManager;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.CredentialValidation;

import javax.ws.rs.core.Response;
import org.keycloak.services.messages.Messages;
import java.util.Optional;

public class UpdatePhoneNumber implements RequiredActionProvider, RequiredActionFactory, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(UpdatePhoneNumber.class);

    private String OTPSecret = null; 
    private String newPhoneNumber = null;
    private String newPhoneNumberLocale = null; 

    @Override
    public InitiatedActionSupport initiatedActionSupport() {
        return InitiatedActionSupport.SUPPORTED;
    }

    @Override
    public String getDisplayText() {
        return "Update Phone Number";
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.challenge(context.form().createResponse(UserModel.RequiredAction.UPDATE_PHONE_NUMBER));
    }

    @Override
    public void processAction(RequiredActionContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String formPhoneNumber = formData.getFirst(UserModel.PHONE_NUMBER);
        String formPhoneNumberLocale = formData.getFirst(UserModel.PHONE_NUMBER_LOCALE);
        RealmModel realm = context.getRealm();

        OTPCredentialModel credentialModel = OTPCredentialModel.createFromSOTPPolicy(realm, OTPSecret, "SMS");
        if(formPhoneNumber != null){
            newPhoneNumber = formPhoneNumber;
        }
        if(formPhoneNumberLocale != null){
            newPhoneNumberLocale = formPhoneNumberLocale;
        }
        String challangeResponse = formData.getFirst("sotp");
        String resendOTP = formData.getFirst("resendOTP");
        System.out.println("Resend OTP? : " + resendOTP);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if(resendOTP != null){
            sendPhoneNumberUpdateConfirmation(context,credentialModel);
            return;
        }

        System.out.println("OTP entered is : " +  challangeResponse);

        if((challangeResponse == null || Validation.isBlank(challangeResponse)) && (formPhoneNumber == null || formPhoneNumberLocale == null)){
            Response challenge = context.form()
                .addError(new FormMessage(Validation.FIELD_SOTP_CODE, Messages.INVALID_TOTP))
                .createResponse(UserModel.RequiredAction.CONFIGURE_SOTP);
            context.challenge(challenge);
            return;
        }

        if(!Validation.isBlank(challangeResponse)){
            System.out.println("OTPSecret is : " + OTPSecret);
            
            OTPPolicy policy = realm.getSmsOTPPolicy();
            boolean isOTPvalid = validateOTPCredential(context,challangeResponse,credentialModel,policy,authSession);
            System.out.println("IS OTP VALID? : " + isOTPvalid);
            if(!isOTPvalid){
                String attemptsRemaining = authSession.getAuthNote(Details.ATTEMPTS_REMAINING);
                if (attemptsRemaining == null || Validation.isBlank(attemptsRemaining)){
                    authSession.setAuthNote(Details.ATTEMPTS_REMAINING, "3");
                }
                else if(Integer.parseInt(attemptsRemaining) == 1){    
                    context.getEvent().error(Errors.ACCESS_DENIED);
                    context.failure();
                    Response challenge = context.form()
                         .setError("Your code could not be verified, please try again.")
                         .createErrorPage(Response.Status.UNAUTHORIZED);
                    context.challenge(challenge);
                    return;
                }
                else{
                    String setAttempts = Integer.toString(Integer.parseInt(attemptsRemaining)-1);
                    authSession.setAuthNote(Details.ATTEMPTS_REMAINING, setAttempts);
                }
                Response challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_SOTP_CODE, Messages.INVALID_TOTP))
                    .createResponse(UserModel.RequiredAction.CONFIGURE_SOTP);
                context.challenge(challenge);
                return;
            }
            // if (!CredentialHelper.createSOTPCredential(context.getSession(), context.getRealm(), context.getUser(), challengeResponse, credentialModel)) {
            //     Response challenge = context.form()
            //             .addError(new FormMessage(Validation.FIELD_SOTP_CODE, Messages.INVALID_TOTP))
            //             .createResponse(UserModel.RequiredAction.CONFIGURE_SOTP);
            //     context.challenge(challenge);
            //     return;
            // }
            context.success();
            return;

        }
        

        UserModel user = context.getUser();
        boolean verifyPhoneNumber = realm.isVerifyPhoneNumber();
        boolean isOverrideNull = (authSession.getAuthNote(Details.OVERRIDE_VERIFICATION) == null);
        System.out.println("Override Verify Phone Number : " + !isOverrideNull);

        System.out.println("Numbers from form " + newPhoneNumber + " - " + newPhoneNumberLocale);

        UserProfile phoneNumberUpdateValidationResult;

        try {
            phoneNumberUpdateValidationResult = validatePhoneNumberUpdate(context.getSession(), user, newPhoneNumber, newPhoneNumberLocale);
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());
            context.challenge(context.form().setErrors(errors).setFormData(formData)
                    .createResponse(UserModel.RequiredAction.UPDATE_PHONE_NUMBER));
            return;
        }

        if ((!realm.isVerifyPhoneNumber() && isOverrideNull) || 
            Objects.equals(user.getPhoneNumber(), newPhoneNumber) && user.isPhoneNumberVerified()) {
            System.out.println("!!!!!!!IMPORTANT!!!!!! CHECK THIS");
            System.out.println("Is Phone Number Verified? : "+ (user.isPhoneNumberVerified()));        
            updatePhoneNumberWithoutConfirmation(context, phoneNumberUpdateValidationResult);
            return;
        }

        sendPhoneNumberUpdateConfirmation(context,credentialModel);
    }

    protected boolean validateOTPCredential(RequiredActionContext context, String token, OTPCredentialModel credentialModel, OTPPolicy policy, AuthenticationSessionModel authSession) {
        return CredentialValidation.validOTP(token, credentialModel, policy.getLookAheadWindow(), authSession);
    }

    private void sendPhoneNumberUpdateConfirmation(RequiredActionContext context, OTPCredentialModel credentialModel) {
        UserModel user = context.getUser();
        String oldPhoneNumber = user.getPhoneNumber();
        // String newPhoneNumber = context.getHttpRequest().getDecodedFormParameters().getFirst(UserModel.PHONE_NUMBER);

        RealmModel realm = context.getRealm();
        int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(UpdatePhoneNumberActionToken.TOKEN_TYPE);

        UriInfo uriInfo = context.getUriInfo();
        KeycloakSession session = context.getSession();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        UpdatePhoneNumberActionToken actionToken = new UpdatePhoneNumberActionToken(user.getId(), Time.currentTime() + validityInSecs,
                oldPhoneNumber, newPhoneNumber);

        int numberOfDigits = realm.getSmsOTPPolicy().getDigits();        
        String otp;

        context.getEvent().event(EventType.SEND_VERIFY_PHONE_NUMBER).detail(Details.PHONE_NUMBER, newPhoneNumber);
        try {
            if(session.getProvider(SMSTemplateProvider.class) == null) System.out.println("!Provider is null");
            if(authenticationSession == null) System.out.println("!Auth session is null");
            System.out.println("Realm : " + realm);
            System.out.println("User : " + user.getUsername());
            
            if(CredentialValidation.isPresentOTPValid(credentialModel, authenticationSession)){
                otp = authenticationSession.getAuthNote(Details.AUTH_OTP);
                long expiry = Long.parseLong(authenticationSession.getAuthNote(Details.OTP_EXPIRY));
                int secondsRemaining = (int)((expiry - Time.currentTimeMillis())/1000);
                long expirationInMinutes = TimeUnit.SECONDS.toMinutes(secondsRemaining);
                session.getProvider(SMSTemplateProvider.class).setAuthenticationSession(authenticationSession).setRealm(realm)
                    .setUser(user).sendPhoneNumberUpdateConfirmation(otp, expirationInMinutes, newPhoneNumber);
            }
            else{
                otp = SecretGenerator.getInstance().randomString(numberOfDigits);
                long absoluteExpiration = Time.currentTimeMillis() + (validityInSecs * 1000);
                authenticationSession.setAuthNote(Details.AUTH_OTP, otp);
                authenticationSession.setAuthNote(Details.OTP_EXPIRY,String.valueOf(absoluteExpiration));
                session.getProvider(SMSTemplateProvider.class).setAuthenticationSession(authenticationSession).setRealm(realm)
                    .setUser(user).sendPhoneNumberUpdateConfirmation(otp, TimeUnit.SECONDS.toMinutes(validityInSecs), newPhoneNumber);
            }
            
            System.out.println("Details : " + otp + "\n" + "Validity Seconds : " + TimeUnit.SECONDS.toMinutes(validityInSecs) + "\nPhone Number : " + newPhoneNumber);
            authenticationSession.setAuthNote(Details.LAST_OTP_SEND,String.valueOf(Time.currentTimeMillis()));
        } catch (SMSException e) {
            logger.error("Failed to send SMS for phone number update", e);
            context.getEvent().error(Errors.SMS_SEND_FAILED);
            return;
        }
        context.getEvent().event(EventType.SEND_VERIFY_PHONE_NUMBER).success();
        // if(messageOnly) return;

        // LoginFormsProvider forms = context.form();
        // context.challenge(forms.setAttribute("messageHeader", forms.getMessage("phoneNumberUpdateConfirmationSentTitle"))
        //         .setInfo("phoneNumberUpdateConfirmationSent", newPhoneNumber).createForm(Templates.getTemplate(LoginFormsPages.LOGIN_CONFIG_SOTP)));
        LoginFormsProvider forms = context.form();
        context.challenge(forms.setAttribute("messageHeader", forms.getMessage("phoneNumberUpdateConfirmationSentTitle"))
                .setInfo("phoneNumberUpdateConfirmationSent", newPhoneNumber).createResponse(UserModel.RequiredAction.CONFIGURE_SOTP));
        return;
    }

    private void updatePhoneNumberWithoutConfirmation(RequiredActionContext context,
                                                UserProfile phoneNumberUpdateValidationResult) {

        updatePhoneNumberNow(context.getEvent(), context.getUser(), phoneNumberUpdateValidationResult);
        context.success();
    }

    public static UserProfile validatePhoneNumberUpdate(KeycloakSession session, UserModel user, String newPhoneNumber, String newPhoneNumberLocale) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle(UserModel.PHONE_NUMBER, newPhoneNumber);
        formData.putSingle(UserModel.PHONE_NUMBER_LOCALE, newPhoneNumberLocale);
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.UPDATE_PHONE_NUMBER, formData, user);
        profile.validate();
        return profile;
    }

    public static void updatePhoneNumberNow(EventBuilder event, UserModel user, UserProfile phoneNumberUpdateValidationResult) {
        String oldPhoneNumber = user.getPhoneNumber();
        String oldPhoneNumberLocale = user.getPhoneNumberLocale();
        String newPhoneNumber = phoneNumberUpdateValidationResult.getAttributes().getFirstValue(UserModel.PHONE_NUMBER);
        String newPhoneNumberLocale = phoneNumberUpdateValidationResult.getAttributes().getFirstValue(UserModel.PHONE_NUMBER_LOCALE);
        System.out.println("Old and new phone number : OLD - "+oldPhoneNumber+" : New - "+newPhoneNumber);
        System.out.println("Old and new phone number : OLD - "+oldPhoneNumberLocale+" : New - "+newPhoneNumberLocale);
        event.event(EventType.UPDATE_PHONE_NUMBER).detail(Details.PREVIOUS_PHONE_NUMBER, oldPhoneNumber).detail(Details.UPDATED_PHONE_NUMBER, newPhoneNumber)
        .detail(Details.PREVIOUS_PHONE_NUMBER_LOCALE, oldPhoneNumberLocale).detail(Details.UPDATED_PHONE_NUMBER_LOCALE, newPhoneNumberLocale);
        phoneNumberUpdateValidationResult.update(false, new EventAuditingAttributeChangeListener(phoneNumberUpdateValidationResult, event));
    }

    private void setStatus(RequiredActionContext context, RequiredActionContext.KcActionStatus status) {
        //TODO: SHOULD I DO THIS?
        AuthenticationManager.setKcActionStatus("UPDATE_PHONE_NUMBER", status, context.getAuthenticationSession());
      }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return UserModel.RequiredAction.UPDATE_PHONE_NUMBER.name();
    }

    @Override
    public boolean isSupported() {
        //TODO: CHECK THIS STUFF
        // return Profile.isFeatureEnabled(Profile.Feature.UPDATE_PHONE_NUMBER);
        return true;
    }
}
