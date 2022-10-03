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

public class UpdatePhoneNumber implements RequiredActionProvider, RequiredActionFactory, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(UpdatePhoneNumber.class);

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
        String newPhoneNumber = formData.getFirst(UserModel.PHONE_NUMBER);
        String newPhoneNumberLocale = formData.getFirst(UserModel.PHONE_NUMBER_LOCALE);

        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        UserProfile phoneNumberUpdateValidationResult;
        try {
            phoneNumberUpdateValidationResult = validatePhoneNumberUpdate(context.getSession(), user, newPhoneNumber, newPhoneNumberLocale);
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());
            context.challenge(context.form().setErrors(errors).setFormData(formData)
                    .createResponse(UserModel.RequiredAction.UPDATE_PHONE_NUMBER));
            return;
        }

        if (!realm.isVerifyPhoneNumber() || Validation.isBlank(newPhoneNumber) || Validation.isBlank(newPhoneNumberLocale)
                || Objects.equals(user.getPhoneNumber(), newPhoneNumber) && user.isPhoneNumberVerified()) {
            System.out.println("!!!!!!!IMPORTANT!!!!!! CHECK THIS");        
            updatePhoneNumberWithoutConfirmation(context, phoneNumberUpdateValidationResult);
            return;
        }

        sendPhoneNumberUpdateConfirmation(context);
    }

    private void sendPhoneNumberUpdateConfirmation(RequiredActionContext context) {
        UserModel user = context.getUser();
        String oldPhoneNumber = user.getPhoneNumber();
        String newPhoneNumber = context.getHttpRequest().getDecodedFormParameters().getFirst(UserModel.PHONE_NUMBER);

        RealmModel realm = context.getRealm();
        int validityInSecs = realm.getActionTokenGeneratedByUserLifespan(UpdatePhoneNumberActionToken.TOKEN_TYPE);

        UriInfo uriInfo = context.getUriInfo();
        KeycloakSession session = context.getSession();
        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();

        UpdatePhoneNumberActionToken actionToken = new UpdatePhoneNumberActionToken(user.getId(), Time.currentTime() + validityInSecs,
                oldPhoneNumber, newPhoneNumber);

        String link = Urls
                .actionTokenBuilder(uriInfo.getBaseUri(), actionToken.serialize(session, realm, uriInfo),
                        authenticationSession.getClient().getClientId(), authenticationSession.getTabId())

                .build(realm.getName()).toString();

        context.getEvent().event(EventType.SEND_VERIFY_PHONE_NUMBER).detail(Details.PHONE_NUMBER, newPhoneNumber);
        try {
            session.getProvider(SMSTemplateProvider.class).setAuthenticationSession(authenticationSession).setRealm(realm)
                    .setUser(user).sendPhoneNumberUpdateConfirmation(link, TimeUnit.SECONDS.toMinutes(validityInSecs), newPhoneNumber);
        } catch (SMSException e) {
            logger.error("Failed to send SMS for phone number update", e);
            context.getEvent().error(Errors.SMS_SEND_FAILED);
            return;
        }
        context.getEvent().success();

        LoginFormsProvider forms = context.form();
        context.challenge(forms.setAttribute("messageHeader", forms.getMessage("phoneNumberUpdateConfirmationSentTitle"))
                .setInfo("phoneNumberUpdateConfirmationSent", newPhoneNumber).createForm(Templates.getTemplate(LoginFormsPages.INFO)));
    }

    private void updatePhoneNumberWithoutConfirmation(RequiredActionContext context,
                                                UserProfile phoneNumberUpdateValidationResult) {

        updatePhoneNumberNow(context.getEvent(), context.getUser(), phoneNumberUpdateValidationResult);
        context.success();
    }

    public static UserProfile validatePhoneNumberUpdate(KeycloakSession session, UserModel user, String newPhoneNumber, String newPhoneNumberLocale) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle(UserModel.PHONE_NUMBER, newPhoneNumber);
        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.UPDATE_PHONE_NUMBER, formData, user);
        profile.validate();
        return profile;
    }

    public static void updatePhoneNumberNow(EventBuilder event, UserModel user, UserProfile phoneNumberUpdateValidationResult) {

        String oldPhoneNumber = user.getPhoneNumber();
        String newPhoneNumber = phoneNumberUpdateValidationResult.getAttributes().getFirstValue(UserModel.PHONE_NUMBER);
        event.event(EventType.UPDATE_PHONE_NUMBER).detail(Details.PREVIOUS_PHONE_NUMBER, oldPhoneNumber).detail(Details.UPDATED_PHONE_NUMBER, newPhoneNumber);
        phoneNumberUpdateValidationResult.update(false, new EventAuditingAttributeChangeListener(phoneNumberUpdateValidationResult, event));
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
        return Profile.isFeatureEnabled(Profile.Feature.UPDATE_PHONE_NUMBER);
    }
}
