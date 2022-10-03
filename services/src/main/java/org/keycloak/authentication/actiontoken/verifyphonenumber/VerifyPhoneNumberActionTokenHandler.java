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

import org.keycloak.authentication.actiontoken.AbstractActionTokenHandler;
import org.keycloak.TokenVerifier.Predicate;
import org.keycloak.authentication.actiontoken.*;
import org.keycloak.events.*;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionCompoundId;
import org.keycloak.sessions.AuthenticationSessionModel;
import java.util.Objects;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Action token handler for verification of e-mail address.
 * @author hmlnarik
 */
public class VerifyPhoneNumberActionTokenHandler extends AbstractActionTokenHandler<VerifyPhoneNumberActionToken> {

    public VerifyPhoneNumberActionTokenHandler() {
        super(
          VerifyPhoneNumberActionToken.TOKEN_TYPE,
          VerifyPhoneNumberActionToken.class,
          Messages.STALE_VERIFY_PHONE_NUMBER_LINK,
          EventType.VERIFY_PHONE_NUMBER,
          Errors.INVALID_TOKEN
        );
    }

    @Override
    public Predicate<? super VerifyPhoneNumberActionToken>[] getVerifiers(ActionTokenContext<VerifyPhoneNumberActionToken> tokenContext) {
        return TokenUtils.predicates(
          TokenUtils.checkThat(
            t -> Objects.equals(t.getPhoneNumber(), tokenContext.getAuthenticationSession().getAuthenticatedUser().getPhoneNumber()),
            Errors.INVALID_PHONE_NUMBER, getDefaultErrorMessage()
          )
        );
    }

    @Override
    public Response handleToken(VerifyPhoneNumberActionToken token, ActionTokenContext<VerifyPhoneNumberActionToken> tokenContext) {
        UserModel user = tokenContext.getAuthenticationSession().getAuthenticatedUser();
        EventBuilder event = tokenContext.getEvent();

        event.event(EventType.VERIFY_PHONE_NUMBER).detail(Details.PHONE_NUMBER, user.getPhoneNumber());

        AuthenticationSessionModel authSession = tokenContext.getAuthenticationSession();
        final UriInfo uriInfo = tokenContext.getUriInfo();
        final RealmModel realm = tokenContext.getRealm();
        final KeycloakSession session = tokenContext.getSession();

        if (tokenContext.isAuthenticationSessionFresh()) {
            // Update the authentication session in the token
            token.setCompoundOriginalAuthenticationSessionId(token.getCompoundAuthenticationSessionId());

            String authSessionEncodedId = AuthenticationSessionCompoundId.fromAuthSession(authSession).getEncodedId();
            token.setCompoundAuthenticationSessionId(authSessionEncodedId);
            UriBuilder builder = Urls.actionTokenBuilder(uriInfo.getBaseUri(), token.serialize(session, realm, uriInfo),
                    authSession.getClient().getClientId(), authSession.getTabId());
            String confirmUri = builder.build(realm.getName()).toString();

            return session.getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(authSession)
                    .setSuccess(Messages.CONFIRM_PHONE_NUMBER_VERIFICATION, user.getPhoneNumber())
                    .setAttribute(Constants.TEMPLATE_ATTR_ACTION_URI, confirmUri)
                    .createInfoPage();
        }

        // verify user phoneNumber as we know it is valid as this entry point would never have gotten here.
        user.setPhoneNumberVerified(true);
        user.removeRequiredAction(RequiredAction.VERIFY_PHONE_NUMBER);
        authSession.removeRequiredAction(RequiredAction.VERIFY_PHONE_NUMBER);

        event.success();

        if (token.getCompoundOriginalAuthenticationSessionId() != null) {
            AuthenticationSessionManager asm = new AuthenticationSessionManager(tokenContext.getSession());
            asm.removeAuthenticationSession(tokenContext.getRealm(), authSession, true);

            return tokenContext.getSession().getProvider(LoginFormsProvider.class)
                    .setAuthenticationSession(authSession)
                    .setSuccess(Messages.PHONE_NUMBER_VERIFIED)
                    .createInfoPage();
        }

        tokenContext.setEvent(event.clone().removeDetail(Details.PHONE_NUMBER).event(EventType.LOGIN));

        String nextAction = AuthenticationManager.nextRequiredAction(session, authSession, tokenContext.getRequest(), event);
        return AuthenticationManager.redirectToRequiredActions(session, realm, authSession, uriInfo, nextAction);
    }

}
