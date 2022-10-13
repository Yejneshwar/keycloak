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

package org.keycloak.models.utils;

import org.keycloak.common.Profile;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.keycloak.common.Profile.isFeatureEnabled;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultRequiredActions {

    /**
     * Check whether the action is the default one used in a realm and is available in the application
     * Often, the default actions can be disabled due to the fact a particular feature is disabled
     *
     * @param action required action
     * @return true if the required action is the default one and is available
     */
    public static boolean isActionAvailable(RequiredActionProviderModel action) {
        if (action == null) return false;
        final Optional<Action> foundAction = Action.findByAlias(action.getAlias());

        return foundAction.isPresent() && foundAction.get().isAvailable();
    }

    /**
     * Add default required actions to the realm
     *
     * @param realm realm
     */
    public static void addActions(RealmModel realm) {
        Arrays.stream(Action.values()).forEach(f -> f.addAction(realm));
    }

    /**
     * Add default required action to the realm
     *
     * @param realm  realm
     * @param action particular required action
     */
    public static void addAction(RealmModel realm, Action action) {
        Optional.ofNullable(action).ifPresent(f -> f.addAction(realm));
    }

    public enum Action {
        VERIFY_EMAIL(UserModel.RequiredAction.VERIFY_EMAIL.name(), DefaultRequiredActions::addVerifyEmailAction),
        VERIFY_PHONE_NUMBER(UserModel.RequiredAction.VERIFY_PHONE_NUMBER.name(), DefaultRequiredActions::addVerifyPhoneNumberAction),
        UPDATE_PROFILE(UserModel.RequiredAction.UPDATE_PROFILE.name(), DefaultRequiredActions::addUpdateProfileAction),
        CONFIGURE_TOTP(UserModel.RequiredAction.CONFIGURE_TOTP.name(), DefaultRequiredActions::addConfigureTotpAction),
        UPDATE_PASSWORD(UserModel.RequiredAction.UPDATE_PASSWORD.name(), DefaultRequiredActions::addUpdatePasswordAction),
        TERMS_AND_CONDITIONS("terms_and_conditions", DefaultRequiredActions::addTermsAndConditionsAction),
        DELETE_ACCOUNT("delete_account", DefaultRequiredActions::addDeleteAccountAction),
        UPDATE_USER_LOCALE("update_user_locale", DefaultRequiredActions::addUpdateLocaleAction),
        UPDATE_PHONE_NUMBER(UserModel.RequiredAction.UPDATE_PASSWORD.name(), DefaultRequiredActions::addUpdatePhoneNumberAction),
        UPDATE_EMAIL(UserModel.RequiredAction.UPDATE_EMAIL.name(), DefaultRequiredActions::addUpdateEmailAction, () -> isFeatureEnabled(Profile.Feature.UPDATE_EMAIL)),
        CONFIGURE_RECOVERY_AUTHN_CODES(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name(), DefaultRequiredActions::addRecoveryAuthnCodesAction, () -> isFeatureEnabled(Profile.Feature.RECOVERY_CODES)),
        WEBAUTHN_REGISTER("webauthn-register", DefaultRequiredActions::addWebAuthnRegisterAction, () -> isFeatureEnabled(Profile.Feature.WEB_AUTHN)),
        WEBAUTHN_PASSWORDLESS_REGISTER("webauthn-register-passwordless", DefaultRequiredActions::addWebAuthnPasswordlessRegisterAction, () -> isFeatureEnabled(Profile.Feature.WEB_AUTHN));

        private final String alias;
        private final Consumer<RealmModel> addAction;
        private final Supplier<Boolean> isAvailable;

        Action(String alias, Consumer<RealmModel> addAction, Supplier<Boolean> isAvailable) {
            this.alias = alias;
            this.addAction = addAction;
            this.isAvailable = isAvailable;
        }

        Action(String alias, Consumer<RealmModel> addAction) {
            this(alias, addAction, () -> true);
        }

        public String getAlias() {
            return alias;
        }

        public void addAction(RealmModel realm) {
            addAction.accept(realm);
        }

        public boolean isAvailable() {
            return isAvailable.get();
        }

        public static Optional<Action> findByAlias(String alias) {
            return Arrays.stream(Action.values())
                    .filter(Objects::nonNull)
                    .filter(f -> f.getAlias().equals(alias))
                    .findFirst();
        }
    }

    public static void addVerifyEmailAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.VERIFY_EMAIL.name()) == null) {
            RequiredActionProviderModel verifyEmail = new RequiredActionProviderModel();
            verifyEmail.setEnabled(true);
            verifyEmail.setAlias(UserModel.RequiredAction.VERIFY_EMAIL.name());
            verifyEmail.setName("Verify Email");
            verifyEmail.setProviderId(UserModel.RequiredAction.VERIFY_EMAIL.name());
            verifyEmail.setDefaultAction(false);
            verifyEmail.setPriority(50);
            realm.addRequiredActionProvider(verifyEmail);

        }
    }

    public static void addVerifyPhoneNumberAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.VERIFY_PHONE_NUMBER.name()) == null) {
            RequiredActionProviderModel verifyPhoneNumber = new RequiredActionProviderModel();
            verifyPhoneNumber.setEnabled(true);
            verifyPhoneNumber.setAlias(UserModel.RequiredAction.VERIFY_PHONE_NUMBER.name());
            verifyPhoneNumber.setName("Verify Phone Number");
            verifyPhoneNumber.setProviderId(UserModel.RequiredAction.VERIFY_PHONE_NUMBER.name());
            verifyPhoneNumber.setDefaultAction(false);
            verifyPhoneNumber.setPriority(49);
            realm.addRequiredActionProvider(verifyPhoneNumber);
        }
    }

    public static void addUpdateProfileAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.UPDATE_PROFILE.name()) == null) {
            RequiredActionProviderModel updateProfile = new RequiredActionProviderModel();
            updateProfile.setEnabled(true);
            updateProfile.setAlias(UserModel.RequiredAction.UPDATE_PROFILE.name());
            updateProfile.setName("Update Profile");
            updateProfile.setProviderId(UserModel.RequiredAction.UPDATE_PROFILE.name());
            updateProfile.setDefaultAction(false);
            updateProfile.setPriority(40);
            realm.addRequiredActionProvider(updateProfile);
        }
    }

    public static void addConfigureTotpAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.CONFIGURE_TOTP.name()) == null) {
            RequiredActionProviderModel totp = new RequiredActionProviderModel();
            totp.setEnabled(true);
            totp.setAlias(UserModel.RequiredAction.CONFIGURE_TOTP.name());
            totp.setName("Configure OTP");
            totp.setProviderId(UserModel.RequiredAction.CONFIGURE_TOTP.name());
            totp.setDefaultAction(false);
            totp.setPriority(10);
            realm.addRequiredActionProvider(totp);
        }
    }

    public static void addUpdatePasswordAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.UPDATE_PASSWORD.name()) == null) {
            RequiredActionProviderModel updatePassword = new RequiredActionProviderModel();
            updatePassword.setEnabled(true);
            updatePassword.setAlias(UserModel.RequiredAction.UPDATE_PASSWORD.name());
            updatePassword.setName("Update Password");
            updatePassword.setProviderId(UserModel.RequiredAction.UPDATE_PASSWORD.name());
            updatePassword.setDefaultAction(false);
            updatePassword.setPriority(30);
            realm.addRequiredActionProvider(updatePassword);
        }
    }

    public static void addTermsAndConditionsAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias("terms_and_conditions") == null) {
            RequiredActionProviderModel termsAndConditions = new RequiredActionProviderModel();
            termsAndConditions.setEnabled(false);
            termsAndConditions.setAlias("terms_and_conditions");
            termsAndConditions.setName("Terms and Conditions");
            termsAndConditions.setProviderId("terms_and_conditions");
            termsAndConditions.setDefaultAction(false);
            termsAndConditions.setPriority(20);
            realm.addRequiredActionProvider(termsAndConditions);
        }
    }

    public static void addUpdatePhoneNumberAction(RealmModel realm){
        if (realm.getRequiredActionProviderByAlias(UserModel.RequiredAction.UPDATE_PHONE_NUMBER.name()) == null
        && Profile.isFeatureEnabled(Profile.Feature.UPDATE_PHONE_NUMBER)) {
            RequiredActionProviderModel updatePhoneNumber = new RequiredActionProviderModel();
            updatePhoneNumber.setEnabled(true);
            updatePhoneNumber.setAlias(UserModel.RequiredAction.UPDATE_PHONE_NUMBER.name());
            updatePhoneNumber.setName("Update Phone Number");
            updatePhoneNumber.setProviderId(UserModel.RequiredAction.UPDATE_PHONE_NUMBER.name());
            updatePhoneNumber.setDefaultAction(false);
            updatePhoneNumber.setPriority(90);
            realm.addRequiredActionProvider(updatePhoneNumber);
        }
    }

    public static void addDeleteAccountAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias("delete_account") == null) {
            RequiredActionProviderModel deleteAccount = new RequiredActionProviderModel();
            deleteAccount.setEnabled(false);
            deleteAccount.setAlias("delete_account");
            deleteAccount.setName("Delete Account");
            deleteAccount.setProviderId("delete_account");
            deleteAccount.setDefaultAction(false);
            deleteAccount.setPriority(60);
            realm.addRequiredActionProvider(deleteAccount);
        }
    }

    public static void addUpdateLocaleAction(RealmModel realm) {
        if (realm.getRequiredActionProviderByAlias("update_user_locale") == null) {
            RequiredActionProviderModel updateUserLocale = new RequiredActionProviderModel();
            updateUserLocale.setEnabled(true);
            updateUserLocale.setAlias("update_user_locale");
            updateUserLocale.setName("Update User Locale");
            updateUserLocale.setProviderId("update_user_locale");
            updateUserLocale.setDefaultAction(false);
            updateUserLocale.setPriority(1000);
            realm.addRequiredActionProvider(updateUserLocale);
        }
    }

    public static void addUpdateEmailAction(RealmModel realm) {
        final String PROVIDER_ID = UserModel.RequiredAction.UPDATE_EMAIL.name();

        final boolean isAvailable = Action.UPDATE_EMAIL.isAvailable();
        if (!isAvailable) return;

        final RequiredActionProviderModel provider = realm.getRequiredActionProviderByAlias(PROVIDER_ID);
        final boolean isRequiredActionActive = provider != null;

        if (!isRequiredActionActive) {
            RequiredActionProviderModel updateEmail = new RequiredActionProviderModel();
            updateEmail.setEnabled(true);
            updateEmail.setAlias(PROVIDER_ID);
            updateEmail.setName("Update Email");
            updateEmail.setProviderId(PROVIDER_ID);
            updateEmail.setDefaultAction(false);
            updateEmail.setPriority(70);
            realm.addRequiredActionProvider(updateEmail);
        }
    }

    public static void addRecoveryAuthnCodesAction(RealmModel realm) {
        final String PROVIDER_ID = UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name();

        final boolean isAvailable = Action.CONFIGURE_RECOVERY_AUTHN_CODES.isAvailable();
        if (!isAvailable) return;

        final RequiredActionProviderModel provider = realm.getRequiredActionProviderByAlias(PROVIDER_ID);
        final boolean isRequiredActionActive = provider != null;

        if (!isRequiredActionActive) {
            RequiredActionProviderModel recoveryCodes = new RequiredActionProviderModel();
            recoveryCodes.setEnabled(true);
            recoveryCodes.setAlias(PROVIDER_ID);
            recoveryCodes.setName("Recovery Authentication Codes");
            recoveryCodes.setProviderId(PROVIDER_ID);
            recoveryCodes.setDefaultAction(false);
            recoveryCodes.setPriority(70);
            realm.addRequiredActionProvider(recoveryCodes);
        }
    }

    public static void addWebAuthnRegisterAction(RealmModel realm) {
        final String PROVIDER_ID = "webauthn-register";

        final boolean isAvailable = Action.WEBAUTHN_REGISTER.isAvailable();
        if (!isAvailable) return;

        final RequiredActionProviderModel provider = realm.getRequiredActionProviderByAlias(PROVIDER_ID);
        final boolean isRequiredActionActive = provider != null;

        if (!isRequiredActionActive) {
            final RequiredActionProviderModel webauthnRegister = new RequiredActionProviderModel();
            webauthnRegister.setEnabled(true);
            webauthnRegister.setAlias(PROVIDER_ID);
            webauthnRegister.setName("Webauthn Register");
            webauthnRegister.setProviderId(PROVIDER_ID);
            webauthnRegister.setDefaultAction(false);
            webauthnRegister.setPriority(70);
            realm.addRequiredActionProvider(webauthnRegister);
        }
    }

    public static void addWebAuthnPasswordlessRegisterAction(RealmModel realm) {
        final String PROVIDER_ID = "webauthn-register-passwordless";

        final boolean isAvailable = Action.WEBAUTHN_PASSWORDLESS_REGISTER.isAvailable();
        if (!isAvailable) return;

        final RequiredActionProviderModel provider = realm.getRequiredActionProviderByAlias(PROVIDER_ID);
        final boolean isRequiredActionActive = provider != null;

        if (!isRequiredActionActive) {
            final RequiredActionProviderModel webauthnRegister = new RequiredActionProviderModel();
            webauthnRegister.setEnabled(true);
            webauthnRegister.setAlias(PROVIDER_ID);
            webauthnRegister.setName("Webauthn Register Passwordless");
            webauthnRegister.setProviderId(PROVIDER_ID);
            webauthnRegister.setDefaultAction(false);
            webauthnRegister.setPriority(80);
            realm.addRequiredActionProvider(webauthnRegister);
        }
    }
}
