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

package org.keycloak.sms.freemarker;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.sms.SMSException;
import org.keycloak.sms.SMSSenderProvider;
import org.keycloak.sms.SMSTemplateProvider;
import org.keycloak.sms.freemarker.beans.EventBean;
import org.keycloak.sms.freemarker.beans.ProfileBean;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.utils.StringUtil;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FreeMarkerSmsTemplateProvider implements SMSTemplateProvider {

    protected KeycloakSession session;
    /**
     * authenticationSession can be null for some sms sendings, it is filled only for sms sendings performed as part of the authentication session (sms verification, password reset, broker link
     * etc.)!
     */
    protected AuthenticationSessionModel authenticationSession;
    protected FreeMarkerProvider freeMarker;
    protected RealmModel realm;
    protected UserModel user;
    protected final Map<String, Object> attributes = new HashMap<>();

    public FreeMarkerSmsTemplateProvider(KeycloakSession session) {
        this.session = session;
        this.freeMarker = session.getProvider(FreeMarkerProvider.class);
    }

    @Override
    public SMSTemplateProvider setRealm(RealmModel realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public SMSTemplateProvider setUser(UserModel user) {
        this.user = user;
        return this;
    }

    @Override
    public SMSTemplateProvider setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public SMSTemplateProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
        this.authenticationSession = authenticationSession;
        return this;
    }

    protected String getRealmName() {
        if (realm.getDisplayName() != null) {
            return realm.getDisplayName();
        } else {
            return ObjectUtil.capitalize(realm.getName());
        }
    }

    @Override
    public void sendEvent(Event event) throws SMSException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", new ProfileBean(user));
        attributes.put("event", new EventBean(event));

        send(toCamelCase(event.getType()) + "Subject", "event-" + event.getType().toString().toLowerCase() + ".ftl", attributes);
    }

    @Override
    public void sendPasswordReset(String link, long expirationInMinutes) throws SMSException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        send("passwordResetSubject", "password-reset.ftl", attributes);
    }

    @Override
    public void sendTestSms(Map<String, String> config, UserModel user) throws SMSException {
        setRealm(session.getContext().getRealm());
        setUser(user);

        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        attributes.put("realmName", realm.getName());

        SMSTemplate sms = processTemplate("smsTestSubject", Collections.emptyList(), "sms-test.ftl", attributes);
        send(config, sms.getSubject(), sms.getTextBody(), sms.getHtmlBody());
    }

    @Override
    public void sendVerifyPhoneNumber(String link, long expirationInMinutes) throws SMSException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        send("phoneNumberVerificationSubject", "phone-number-verification.ftl", attributes);
    }

    @Override
    public void sendPhoneNumberUpdateConfirmation(String link, long expirationInMinutes, String newPhoneNumber) throws SMSException {
        if (newPhoneNumber == null) {
            throw new IllegalArgumentException("The new sms is mandatory");
        }

        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("user", new ProfileBean(user));
        attributes.put("newPhoneNumber", newPhoneNumber);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        attributes.put("realmName", getRealmName());

        send("phoneNumberUpdateConfirmationSubject", Collections.emptyList(), "phone-number-update-confirmation.ftl", attributes, newPhoneNumber);
    }

    /**
     * Add link info into template attributes.
     * 
     * @param link to add
     * @param expirationInMinutes to add
     * @param attributes to add link info into
     */
    protected void addLinkInfoIntoAttributes(String link, long expirationInMinutes, Map<String, Object> attributes) throws SMSException {
        attributes.put("link", link);
        attributes.put("linkExpiration", expirationInMinutes);
        KeycloakUriInfo uriInfo = session.getContext().getUri();
        try {
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(getTheme().getMessages(locale), locale));
            attributes.put("url", new UrlBean(realm, getTheme(), uriInfo.getBaseUri(), null));
        } catch (IOException e) {
            throw new SMSException("Failed to template sms", e);
        }
    }

    @Override
    public void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes) throws SMSException {
        send(subjectFormatKey, Collections.emptyList(), bodyTemplate, bodyAttributes);
    }

    protected SMSTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws SMSException {
        try {
            Theme theme = getTheme();
            Locale locale = session.getContext().resolveLocale(user);
            attributes.put("locale", locale);
            Properties rb = new Properties();
            if(!StringUtil.isNotBlank(realm.getDefaultLocale()))
            {
                rb.putAll(realm.getRealmLocalizationTextsByLocale(realm.getDefaultLocale()));
            }
            rb.putAll(theme.getMessages(locale));
            rb.putAll(realm.getRealmLocalizationTextsByLocale(locale.toLanguageTag()));
            attributes.put("msg", new MessageFormatterMethod(locale, rb));
            attributes.put("properties", theme.getProperties());
            String subject = new MessageFormat(rb.getProperty(subjectKey, subjectKey), locale).format(subjectAttributes.toArray());
            String textTemplate = String.format("text/%s", template);
            String textBody;
            try {
                textBody = freeMarker.processTemplate(attributes, textTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new SMSException("Failed to template plain text sms.", e);
            }
            String htmlTemplate = String.format("html/%s", template);
            String htmlBody;
            try {
                htmlBody = freeMarker.processTemplate(attributes, htmlTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new SMSException("Failed to template html sms.", e);
            }

            return new SMSTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new SMSException("Failed to template sms", e);
        }
    }

    protected Theme getTheme() throws IOException {
        return session.theme().getTheme(Theme.Type.SMS);
    }

    @Override
    public void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes) throws SMSException {
        send(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes, null);
    }

    protected void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes, String address) throws SMSException {
        try {
            SMSTemplate sms = processTemplate(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes);
            send(sms.getSubject(), sms.getTextBody(), sms.getHtmlBody(), address);
        } catch (SMSException e) {
            throw e;
        } catch (Exception e) {
            throw new SMSException("Failed to template sms", e);
        }
    }

    protected void send(String subject, String textBody, String htmlBody, String address) throws SMSException {
        send(realm.getSmtpConfig(), subject, textBody, htmlBody, address);
    }

    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody) throws SMSException {
        send(config, subject, textBody, htmlBody, null);
    }

    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody, String address) throws SMSException {
        SMSSenderProvider smsSender = session.getProvider(SMSSenderProvider.class);
        if (address == null) {
            smsSender.send(config, user, subject, textBody, htmlBody);
        } else {
            smsSender.send(config, address, subject, textBody, htmlBody);
        }
    }

    @Override
    public void close() {
    }

    protected String toCamelCase(EventType event) {
        StringBuilder sb = new StringBuilder("event");
        for (String s : event.name().toLowerCase().split("_")) {
            sb.append(ObjectUtil.capitalize(s));
        }
        return sb.toString();
    }

    protected static class SMSTemplate {

        private String subject;
        private String textBody;
        private String htmlBody;

        public SMSTemplate(String subject, String textBody, String htmlBody) {
            this.subject = subject;
            this.textBody = textBody;
            this.htmlBody = htmlBody;
        }

        public String getSubject() {
            return subject;
        }

        public String getTextBody() {
            return textBody;
        }

        public String getHtmlBody() {
            return htmlBody;
        }
    }

}
