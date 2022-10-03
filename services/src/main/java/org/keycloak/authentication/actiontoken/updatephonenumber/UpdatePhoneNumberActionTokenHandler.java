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

package org.keycloak.authentication.actiontoken.updatephonenumber;

import java.util.List;
import java.util.Objects;
import javax.ws.rs.core.Response;
import org.keycloak.TokenVerifier;
import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.authentication.actiontoken.ActionTokenContext;
import org.keycloak.authentication.actiontoken.TokenUtils;
import org.keycloak.authentication.requiredactions.UpdatePhoneNumber;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.ValidationException;

public class UpdatePhoneNumberActionTokenHandler extends AbstractActionTokenHandler<UpdatePhoneNumberActionToken> {

    public UpdatePhoneNumberActionTokenHandler() {
        super(UpdatePhoneNumberActionToken.TOKEN_TYPE, UpdatePhoneNumberActionToken.class, Messages.STALE_VERIFY_PHONE_NUMBER_LINK,
                EventType.EXECUTE_ACTIONS, Errors.INVALID_TOKEN);
    }

    @Override
    public TokenVerifier.Predicate<? super UpdatePhoneNumberActionToken>[] getVerifiers(
            ActionTokenContext<UpdatePhoneNumberActionToken> tokenContext) {
        return TokenUtils.predicates(TokenUtils.checkThat(
                t -> Objects.equals(t.getOldPhoneNumber(), tokenContext.getAuthenticationSession().getAuthenticatedUser().getPhoneNumber()),
                Errors.INVALID_PHONE_NUMBER, getDefaultErrorMessage()));
    }

    @Override
    public Response handleToken(UpdatePhoneNumberActionToken token, ActionTokenContext<UpdatePhoneNumberActionToken> tokenContext) {
        AuthenticationSessionModel authenticationSession = tokenContext.getAuthenticationSession();
        UserModel user = authenticationSession.getAuthenticatedUser();

        KeycloakSession session = tokenContext.getSession();

        LoginFormsProvider forms = session.getProvider(LoginFormsProvider.class).setAuthenticationSession(authenticationSession)
                .setUser(user);

        String newPhoneNumber = token.getNewPhoneNumber();
        String newPhoneNumberLocale = token.getNewPhoneNumberLocale();

        UserProfile phoneNumberUpdateValidationResult;
        try {
            phoneNumberUpdateValidationResult = UpdatePhoneNumber.validatePhoneNumberUpdate(session, user, newPhoneNumber, newPhoneNumberLocale);
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());
            return forms.setErrors(errors).createErrorPage(Response.Status.BAD_REQUEST);
        }

        UpdatePhoneNumber.updatePhoneNumberNow(tokenContext.getEvent(), user, phoneNumberUpdateValidationResult);

        tokenContext.getEvent().success();

        // verify user phoneNumber as we know it is valid as this entry point would never have gotten here.
        user.setPhoneNumberVerified(true);
        user.removeRequiredAction(UserModel.RequiredAction.UPDATE_PHONE_NUMBER);
        tokenContext.getAuthenticationSession().removeRequiredAction(UserModel.RequiredAction.UPDATE_PHONE_NUMBER);
        user.removeRequiredAction(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);
        tokenContext.getAuthenticationSession().removeRequiredAction(UserModel.RequiredAction.VERIFY_PHONE_NUMBER);

        return forms.setAttribute("messageHeader", forms.getMessage("phoneNumberUpdatedTitle")).setSuccess("phoneNumberUpdated", newPhoneNumber)
                .createInfoPage();
    }

    @Override
    public boolean canUseTokenRepeatedly(UpdatePhoneNumberActionToken token,
            ActionTokenContext<UpdatePhoneNumberActionToken> tokenContext) {
        return false;
    }
}
