/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.map.realm.entity;

import org.keycloak.models.OTPPolicy;
import org.keycloak.models.map.annotations.GenerateEntityImplementations;
import org.keycloak.models.map.common.DeepCloner;
import org.keycloak.models.map.common.UpdatableEntity;

@GenerateEntityImplementations
@DeepCloner.Root
public interface MapSmsOTPPolicyEntity extends UpdatableEntity {
    static MapSmsOTPPolicyEntity fromModel(OTPPolicy model) {
        if (model == null) return null;
        MapSmsOTPPolicyEntity entity = new MapSmsOTPPolicyEntityImpl();
        entity.setSmsOtpPolicyAlgorithm(model.getAlgorithm());
        entity.setSmsOtpPolicyDigits(model.getDigits());
        entity.setSmsOtpPolicyInitialCounter(model.getInitialCounter());
        entity.setSmsOtpPolicyLookAheadWindow(model.getLookAheadWindow());
        entity.setSmsOtpPolicyType(model.getType());
        entity.setSmsOtpPolicyPeriod(model.getPeriod());
        return entity;
    }

    static OTPPolicy toModel(MapSmsOTPPolicyEntity entity) {
        if (entity == null) return null;
        OTPPolicy model = new OTPPolicy();
        Integer smsOtpPolicyDigits = entity.getSmsOtpPolicyDigits();
        model.setDigits(smsOtpPolicyDigits == null ? 0 : smsOtpPolicyDigits);
        model.setAlgorithm(entity.getSmsOtpPolicyAlgorithm());
        Integer smsOtpPolicyInitialCounter = entity.getSmsOtpPolicyInitialCounter();
        model.setInitialCounter(smsOtpPolicyInitialCounter == null ? 0 : smsOtpPolicyInitialCounter);
        Integer smsOtpPolicyLookAheadWindow = entity.getSmsOtpPolicyLookAheadWindow();
        model.setLookAheadWindow(smsOtpPolicyLookAheadWindow == null ? 0 : smsOtpPolicyLookAheadWindow);
        model.setType(entity.getSmsOtpPolicyType());
        Integer smsOtpPolicyPeriod = entity.getSmsOtpPolicyPeriod();
        model.setPeriod(smsOtpPolicyPeriod == null ? 0 : smsOtpPolicyPeriod);
        return model;
    }

    Integer getSmsOtpPolicyInitialCounter();
    void setSmsOtpPolicyInitialCounter(Integer smsOtpPolicyInitialCounter);

    Integer getSmsOtpPolicyDigits();
    void setSmsOtpPolicyDigits(Integer smsOtpPolicyDigits);

    Integer getSmsOtpPolicyLookAheadWindow();
    void setSmsOtpPolicyLookAheadWindow(Integer smsOtpPolicyLookAheadWindow);

    Integer getSmsOtpPolicyPeriod();
    void setSmsOtpPolicyPeriod(Integer smsOtpPolicyPeriod);

    String getSmsOtpPolicyType();
    void setSmsOtpPolicyType(String smsOtpPolicyType);

    String getSmsOtpPolicyAlgorithm();
    void setSmsOtpPolicyAlgorithm(String smsOtpPolicyAlgorithm);
}
