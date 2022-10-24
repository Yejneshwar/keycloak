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
package org.keycloak.validate.validators;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.keycloak.provider.ConfiguredProvider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.validate.AbstractSimpleValidator;
import org.keycloak.validate.ValidationContext;
import org.keycloak.validate.ValidationError;
import org.keycloak.validate.ValidatorConfig;
import org.keycloak.util.PhoneNumber;

import java.util.Map;

import java.util.Collection;

/**
 * Email format validation - accepts plain string and collection of strings, for basic behavior like null/blank values
 * handling and collections support see {@link AbstractStringValidator}.
 */
public class PhoneNumberValidator extends AbstractSimpleValidator implements ConfiguredProvider {

    public static final String ID = "phoneNumber";

    public static final PhoneNumberValidator INSTANCE = new PhoneNumberValidator();

    public static final String MESSAGE_INVALID_PHONE_NUMBER = "error-invalid-phone-number";


    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected void doValidate(Object value, String inputHint, ValidationContext context, ValidatorConfig config) {
        System.out.println("VALIDATE PHONE NUMBER");
        Map<String,Object> attr = context.getAttributes();

        @SuppressWarnings("unchecked")
        Collection<Object> values = (Collection<Object>) value;

        attr.forEach( (k,v) -> System.out.println("Key: " + k + ": Value: " + v));
        System.out.println(attr.get("org.keycloak.models.UserModel"));
        if(values.size() != 2){
            context.addError(new ValidationError(ID, inputHint, MESSAGE_INVALID_PHONE_NUMBER, value));
            return;
        }
        String locale = null;
        String phoneNumber = null;
        System.out.println("To validate : ");
        for (Object val : values) {
            System.out.println("val : " + val.toString());
            if(val.toString().length() == 2){
                locale = val.toString();
            }
            else{
                phoneNumber = val.toString();
            }
        }
        if (!PhoneNumber.validatePhoneNumber(phoneNumber,locale)) {
            context.addError(new ValidationError(ID, inputHint, MESSAGE_INVALID_PHONE_NUMBER, value));
        }
    }

    @Override
    protected boolean skipValidation(Object value, ValidatorConfig config) {
        if (isIgnoreEmptyValuesConfigured(config) && (value == null || value instanceof String)) {
            return  value == null || "".equals(value.toString());
        }
        return false;
    }
    
    @Override
    public String getHelpText() {
        return "phone number validator";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }
}
