package org.keycloak.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class PhoneNumber {
    public static boolean validatePhoneNumber(String phoneNumber, String locale){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        System.out.println("Validating phone Number");
        if (locale.length() > 2 ) return false;

        return false;
    } 

}
