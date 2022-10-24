package org.keycloak.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;

public class PhoneNumber {
    public static boolean validatePhoneNumber(String phoneNumber, String locale) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        System.out.println("Validating phone Number");
        System.out.println("phone number : " + phoneNumber);
        System.out.println("locale : " + locale);

        if (locale == null || locale.length() != 2) return false;

        Phonenumber.PhoneNumber phoneNumberProto;
        try {
            phoneNumberProto = phoneNumberUtil.parseAndKeepRawInput(phoneNumber, locale);
        } catch (NumberParseException e) {
            throw new RuntimeException(e);
        }

        CountryCodeSource countrySource = phoneNumberProto.getCountryCodeSource();

        if(countrySource.equals(CountryCodeSource.UNSPECIFIED)) return false;
        if(countrySource.equals(CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN) || countrySource.equals(CountryCodeSource.FROM_NUMBER_WITHOUT_PLUS_SIGN) || countrySource.equals(CountryCodeSource.FROM_NUMBER_WITH_IDD)){
            long nationalNumber = phoneNumberProto.getNationalNumber();
            try {
                Phonenumber.PhoneNumber nationalNumberProto = phoneNumberUtil.parseAndKeepRawInput(Long.toString(nationalNumber), locale);
                if(!nationalNumberProto.getCountryCodeSource().equals(CountryCodeSource.FROM_DEFAULT_COUNTRY)) return false;
            }
            catch(NumberParseException e) {
                throw new RuntimeException(e);
            }
        }

        if(phoneNumberUtil.isValidNumber(phoneNumberProto) && phoneNumberUtil.isValidNumberForRegion(phoneNumberProto,locale)) return true;
        return false;
    }
}
