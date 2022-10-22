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

import com.sun.mail.smtp.SMTPMessage;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.truststore.HostnameVerificationPolicy;
import org.keycloak.truststore.JSSETruststoreConfigurator;
import org.keycloak.vault.VaultStringSecret;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultSmsSenderProvider implements SMSSenderProvider {

    private static final String SUPPORTED_SSL_PROTOCOLS = getSupportedSslProtocols();

    private static final Logger logger = Logger.getLogger(DefaultSmsSenderProvider.class);

    private final KeycloakSession session;

    public DefaultSmsSenderProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void send(Map<String, String> config, UserModel user, String subject, String textBody) throws SMSException {
        send(config, retrievePhoneNumber(user), subject, textBody);
    }

    @Override
    public void send(Map<String, String> config, String address, String subject, String textBody) throws SMSException {
        Transport transport = null;
        try {

            Properties props = new Properties();

            if (config.containsKey("host")) {
                props.setProperty("mail.smtp.host", config.get("host"));
            }

            boolean auth = "true".equals(config.get("auth"));
            boolean ssl = "true".equals(config.get("ssl"));
            boolean starttls = "true".equals(config.get("starttls"));

            if (config.containsKey("port") && config.get("port") != null) {
                props.setProperty("mail.smtp.port", config.get("port"));
            }

            if (auth) {
                props.setProperty("mail.smtp.auth", "true");
            }

            if (ssl) {
                props.setProperty("mail.smtp.ssl.enable", "true");
            }

            if (starttls) {
                props.setProperty("mail.smtp.starttls.enable", "true");
            }

            if (ssl || starttls) {
                props.put("mail.smtp.ssl.protocols", SUPPORTED_SSL_PROTOCOLS);

                setupTruststore(props);
            }

            props.setProperty("mail.smtp.timeout", "10000");
            props.setProperty("mail.smtp.connectiontimeout", "10000");

            String from = config.get("from");
            String fromDisplayName = config.get("fromDisplayName");
            String replyTo = config.get("replyTo");
            String replyToDisplayName = config.get("replyToDisplayName");
            String envelopeFrom = config.get("envelopeFrom");

            Session session = Session.getInstance(props);

            Multipart multipart = new MimeMultipart("alternative");

            if (textBody != null) {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(textBody, "UTF-8");
                multipart.addBodyPart(textPart);
            }

            SMTPMessage msg = new SMTPMessage(session);

            System.out.println("SEND TO : " + address);
            System.out.println("Subject : " + subject);
            System.out.println("Content : " + textBody);

            msg.setHeader("To", address);
            msg.setSubject(subject, "utf-8");
            msg.setContent(multipart);
            msg.saveChanges();
            msg.setSentDate(new Date());

        } catch (Exception e) {
            ServicesLogger.LOGGER.failedToSendSms(e);
            throw new SMSException(e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    logger.warn("Failed to close transport", e);
                }
            }
        }
    }

    protected InternetAddress toInternetAddress(String phoneNumber, String displayName) throws UnsupportedEncodingException, AddressException, SMSException {
        if (phoneNumber == null || "".equals(phoneNumber.trim())) {
            throw new SMSException("Please provide a valid address", null);
        }
        if (displayName == null || "".equals(displayName.trim())) {
            return new InternetAddress(phoneNumber);
        }
        return new InternetAddress(phoneNumber, displayName, "utf-8");
    }

    protected String retrievePhoneNumber(UserModel user) {
        return user.getPhoneNumber();
    }

    private void setupTruststore(Properties props) {
        boolean checkServerIdentity = true;

        JSSETruststoreConfigurator configurator = new JSSETruststoreConfigurator(session);

        SSLSocketFactory factory = configurator.getSSLSocketFactory();
        if (factory != null) {
            props.put("mail.smtp.ssl.socketFactory", factory);
            if (configurator.getProvider().getPolicy() == HostnameVerificationPolicy.ANY) {
                props.setProperty("mail.smtp.ssl.trust", "*");
                checkServerIdentity = false;
            }
        }

        if (checkServerIdentity) {
            props.put("mail.smtp.ssl.checkserveridentity", "true");
        }
    }

    @Override
    public void close() {

    }

    private static String getSupportedSslProtocols() {
        try {
            String[] protocols = SSLContext.getDefault().getSupportedSSLParameters().getProtocols();
            if (protocols != null) {
                return String.join(" ", protocols);
            }
        } catch (Exception e) {
            logger.warn("Failed to get list of supported SSL protocols", e);
        }
        return null;
    }

}
