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

package org.keycloak.authentication.requiredactions;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.*;
import org.keycloak.authentication.actiontoken.verifyphonenumber.VerifyPhoneNumberActionToken;
import org.keycloak.common.util.Time;
import org.keycloak.sms.SMSException;
import org.keycloak.sms.SMSTemplateProvider;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.services.Urls;
import org.keycloak.services.validation.Validation;

import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.*;

import org.keycloak.common.util.SecretGenerator;

import org.keycloak.models.OTPPolicy;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.utils.CredentialValidation;
import org.keycloak.services.messages.Messages;
import org.keycloak.models.utils.FormMessage;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class VerifyPhoneNumber implements RequiredActionProvider, RequiredActionFactory {
    private static final Logger logger = Logger.getLogger(VerifyPhoneNumber.class);
    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        if (context.getRealm().isVerifyPhoneNumber() && !context.getUser().isPhoneNumberVerified()) {
            context.getUser().addRequiredAction(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
            logger.debug("User is required to verify phonenumber");
        }
    }
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        String OTPSecret = null; 
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        if (context.getUser().isPhoneNumberVerified()) {
            context.success();
            authSession.removeAuthNote(Constants.VERIFY_PHONE_NUMBER_KEY);
            return;
        }

        String phoneNumber = context.getUser().getPhoneNumber();
        if (Validation.isBlank(phoneNumber)) {
            context.ignore();
            return;
        }

        LoginFormsProvider loginFormsProvider = context.form();
        Response challenge;

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        RealmModel realm = context.getRealm();
        OTPCredentialModel credentialModel = OTPCredentialModel.createFromSOTPPolicy(realm, OTPSecret, "SMS");

        String resendOTP = formData.getFirst("resendOTP");
        System.out.println("Resend OTP? : " + resendOTP);
        if(resendOTP != null){
            logger.debugf("Re-sending otp requested for user: %s", context.getUser().getUsername());
            EventBuilder event = context.getEvent().clone().event(EventType.SEND_VERIFY_PHONE_NUMBER).detail(Details.PHONE_NUMBER, phoneNumber);
            challenge = sendVerifyPhoneNumber(context.getSession(), loginFormsProvider, context.getUser(), context.getAuthenticationSession(), credentialModel, event);
            context.challenge(challenge);
            return;
        }

        authSession.setClientNote(AuthorizationEndpointBase.APP_INITIATED_FLOW, null);

        // Do not allow resending e-mail by simple page refresh, i.e. when e-mail sent, it should be resent properly via email-verification endpoint
        if (! Objects.equals(authSession.getAuthNote(Constants.VERIFY_PHONE_NUMBER_KEY), phoneNumber)) {
            authSession.setAuthNote(Constants.VERIFY_PHONE_NUMBER_KEY, phoneNumber);
            EventBuilder event = context.getEvent().clone().event(EventType.SEND_VERIFY_PHONE_NUMBER).detail(Details.PHONE_NUMBER, phoneNumber);
            challenge = sendVerifyPhoneNumber(context.getSession(), loginFormsProvider, context.getUser(), context.getAuthenticationSession(), credentialModel, event);
        } else {
            String challangeResponse = formData.getFirst("otp");

            if((challangeResponse == null || Validation.isBlank(challangeResponse))){
                challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_OTP_CODE, Messages.MISSING_TOTP))
                    .createResponse(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
                context.challenge(challenge);
                return;
            }

            OTPPolicy policy = realm.getSmsOTPPolicy();

            boolean isOTPvalid = validateOTPCredential(context,challangeResponse,credentialModel,policy,authSession);
            System.out.println("IS OTP VALID? : " + isOTPvalid);

            if(isOTPvalid){
                context.success();
                authSession.removeAuthNote(Constants.VERIFY_PHONE_NUMBER_KEY);
                return;   
            }
            else{
                String attemptsRemaining = authSession.getAuthNote(Details.ATTEMPTS_REMAINING);
                if (attemptsRemaining == null || Validation.isBlank(attemptsRemaining)){
                    authSession.setAuthNote(Details.ATTEMPTS_REMAINING, "3");
                }
                else if(Integer.parseInt(attemptsRemaining) == 1){
                    authSession.removeAuthNote(Details.AUTH_OTP);
                    context.getEvent().error(Errors.ACCESS_DENIED);
                    context.failure();
                    // challenge = context.form()
                    //      .setError("Your code could not be verified, please try again.")
                    //      .createErrorPage(Response.Status.UNAUTHORIZED);
                    // context.challenge(challenge);
                    return;
                }
                else{
                    String setAttempts = Integer.toString(Integer.parseInt(attemptsRemaining)-1);
                    authSession.setAuthNote(Details.ATTEMPTS_REMAINING, setAttempts);
                }
                // challenge = loginFormsProvider.setAttribute("messageHeader", loginFormsProvider.getMessage("phoneNumberUpdateConfirmationSentTitle"))
                // .setInfo("phoneNumberUpdateConfirmationSent", phoneNumber).createResponse(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
                challenge = context.form()
                    .addError(new FormMessage(Validation.FIELD_OTP_CODE, Messages.INVALID_TOTP))
                    .createResponse(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
            }


        }

        context.challenge(challenge);
    }

    protected boolean validateOTPCredential(RequiredActionContext context, String token, OTPCredentialModel credentialModel, OTPPolicy policy, AuthenticationSessionModel authSession) {
        return CredentialValidation.validOTP(token, credentialModel, policy.getLookAheadWindow(), authSession);
    }


    @Override
    public void processAction(RequiredActionContext context) {
        requiredActionChallenge(context);
    }


    @Override
    public void close() {

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
    public String getDisplayText() {
        return "Verify Phone Number";
    }


    @Override
    public String getId() {
        return UserModel.RequiredAction.VERIFY_PHONE_NUMBER.name();
    }

    private Response sendVerifyPhoneNumber(KeycloakSession session, LoginFormsProvider forms, UserModel user, AuthenticationSessionModel authSession, OTPCredentialModel credentialModel, EventBuilder event) throws UriBuilderException, IllegalArgumentException {
        RealmModel realm = session.getContext().getRealm();
        UriInfo uriInfo = session.getContext().getUri();

        int validityInSecs = realm.getSmsOTPPolicy().getPeriod();
        int numberOfDigits = realm.getSmsOTPPolicy().getDigits();
        
        // String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
        // VerifyPhoneNumberActionToken token = new VerifyPhoneNumberActionToken(user.getId(), absoluteExpirationInSecs, authSessionEncodedId, user.getPhoneNumber(), authSession.getClient().getClientId());
        long absoluteExpiration = Time.currentTimeMillis() + (validityInSecs * 1000);
        String otp;
        long expirationInMinutes;
        if(CredentialValidation.isPresentOTPValid(credentialModel, authSession)){
            otp = authSession.getAuthNote(Details.AUTH_OTP);
            long expiry = Long.parseLong(authSession.getAuthNote(Details.OTP_EXPIRY));
            int secondsRemaining = (int)((expiry - Time.currentTimeMillis())/1000);
            expirationInMinutes = TimeUnit.SECONDS.toMinutes(secondsRemaining);
        }
        else{
            System.out.println("Validity : " + validityInSecs);
            System.out.println("Time set :" + String.valueOf(absoluteExpiration));
            System.out.println("Get current time :" + Time.currentTimeMillis());
            System.out.println("Get current time + validity :" + (Time.currentTimeMillis() + (validityInSecs * 1000)));
    
            otp = SecretGenerator.getInstance().randomString(numberOfDigits);
            expirationInMinutes = TimeUnit.SECONDS.toMinutes(validityInSecs);
            authSession.setAuthNote(Details.AUTH_OTP, otp);
            authSession.setAuthNote(Details.OTP_EXPIRY,String.valueOf(absoluteExpiration));
        }


        String phoneNumber = user.getPhoneNumber();
        String phoneNumberHidden = "*******"+phoneNumber.substring(phoneNumber.length() - 4);

        try {
            session
              .getProvider(SMSTemplateProvider.class)
              .setAuthenticationSession(authSession)
              .setRealm(realm)
              .setUser(user)
              .sendVerifyPhoneNumber(otp, expirationInMinutes);
            event.success();
            String time = String.valueOf(Time.currentTimeMillis());
            System.out.println("OTP send Time : "+time);
            authSession.setAuthNote(Details.LAST_OTP_SEND,time);
        } catch (SMSException e) {
            logger.error("Failed to send verification SMS", e);
            authSession.removeAuthNote(Details.AUTH_OTP);
            event.error(Errors.SMS_SEND_FAILED);
        }

        return forms.setAttribute("messageHeader", forms.getMessage("phoneNumberUpdateConfirmationSentTitle"))
        .setInfo("phoneNumberUpdateConfirmationSent", phoneNumberHidden).createResponse(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
    }
}
