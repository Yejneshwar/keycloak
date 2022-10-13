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

package org.keycloak.sms;

import org.keycloak.events.Event;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface SMSTemplateProvider extends Provider {

    String IDENTITY_PROVIDER_BROKER_CONTEXT = "identityProviderBrokerCtx";
    
    SMSTemplateProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession);

    SMSTemplateProvider setRealm(RealmModel realm);

    SMSTemplateProvider setUser(UserModel user);

    SMSTemplateProvider setAttribute(String name, Object value);

    void sendEvent(Event event) throws SMSException;

    /**
     * Reset password sent from forgot password link on login
     *
     * @param link
     * @param expirationInMinutes
     * @throws SMSException
     */
    void sendPasswordReset(String link, long expirationInMinutes) throws SMSException;

    /**
     * Test SMTP connection with current logged in user
     *
     * @param config SMTP server configuration
     * @param user SMTP recipient
     * @throws SMSException
     */
    void sendTestSms(Map<String, String> config, UserModel user) throws SMSException;

    void sendVerifyPhoneNumber(String link, long expirationInMinutes) throws SMSException;

    void sendPhoneNumberUpdateConfirmation(String link, long expirationInMinutes, String address) throws SMSException;

    /**
     * Send formatted SMS
     *
     * @param subjectFormatKey message property that will be used to format SMS subject
     * @param bodyTemplate freemarker template file
     * @param bodyAttributes attributes used to fill template
     * @throws SMSException
     */
    void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes) throws SMSException;

    /**
     * Send formatted SMS
     *
     * @param subjectFormatKey message property that will be used to format SMS subject
     * @param subjectAttributes attributes used to fill subject format message
     * @param bodyTemplate freemarker template file
     * @param bodyAttributes attributes used to fill template
     * @throws SMSException
     */
    void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes) throws SMSException;
}
