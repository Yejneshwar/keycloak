package org.keycloak.models.map.storage.hotRod.realm.entity;

import org.infinispan.protostream.annotations.ProtoField;
import org.keycloak.models.map.annotations.GenerateHotRodEntityImplementation;
import org.keycloak.models.map.storage.hotRod.common.AbstractHotRodEntity;

@GenerateHotRodEntityImplementation(
        implementInterface = "org.keycloak.models.map.realm.entity.MapSmsOTPPolicyEntity"
)
public class HotRodSmsOTPPolicyEntity extends AbstractHotRodEntity {
    @ProtoField(number = 1)
    public Integer smsOtpPolicyDigits;
    @ProtoField(number = 2)
    public Integer smsOtpPolicyInitialCounter;
    @ProtoField(number = 3)
    public Integer smsOtpPolicyLookAheadWindow;
    @ProtoField(number = 4)
    public Integer smsOtpPolicyPeriod;
    @ProtoField(number = 5)
    public String smsOtpPolicyAlgorithm;
    @ProtoField(number = 6)
    public String smsOtpPolicyType;
    @Override
    public boolean equals(Object o) {
        return HotRodSmsOTPPolicyEntityDelegate.entityEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HotRodSmsOTPPolicyEntityDelegate.entityHashCode(this);
    }
}
