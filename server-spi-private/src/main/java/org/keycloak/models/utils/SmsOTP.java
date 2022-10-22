package org.keycloak.models.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.events.Details;
import org.keycloak.common.util.Time;

/**
 * 
 *
 * @author Yejneshwar Sivamoorthy
 * 
 *
 */

public class SmsOTP extends HmacOTP{
    public static final int DEFAULT_INTERVAL_SECONDS = 30;
    public static final int DEFAULT_DELAY_WINDOW = 1;

    private Clock clock;

    public SmsOTP() {
        this(DEFAULT_ALGORITHM, DEFAULT_NUMBER_DIGITS, DEFAULT_INTERVAL_SECONDS, DEFAULT_DELAY_WINDOW);
    }

    /**
     * @param algorithm the encryption algorithm
     * @param numberDigits the number of digits for tokens
     * @param timeIntervalInSeconds the number of seconds a token is valid
     * @param lookAheadWindow the number of previous intervals that should be used to validate tokens.
     */
    public SmsOTP(String algorithm, int numberDigits, int timeIntervalInSeconds, int lookAheadWindow) {
        super(numberDigits, algorithm, lookAheadWindow);
        this.clock = new Clock(timeIntervalInSeconds);
    }

    /**
     * <p>Generates a token.</p>
     *
     * @param secretKey the secret key to derive the token from.
     */
    public String generateSOTP(String secretKey) {
        long T = this.clock.getCurrentInterval();

        String steps = Long.toHexString(T).toUpperCase();

        // Just get a 16 digit string
        while (steps.length() < 16)
            steps = "0" + steps;

        return generateOTP(secretKey, steps, this.numberDigits, this.algorithm);
    }

    public boolean isPresentOTPValid(AuthenticationSessionModel authSession){
        System.out.println("isPresentOTPValid?");
        String expiry = authSession.getAuthNote(Details.OTP_EXPIRY);
        String presentOTP = authSession.getAuthNote(Details.AUTH_OTP);
        if (presentOTP == null || expiry == null) return false;
        if (presentOTP.length() != this.numberDigits) return false;
        //accounting for request times and such
        if ((Long.parseLong(expiry) - Time.currentTimeMillis()) <= 1) return false;
        return true;

    }

    /**
     * <p>Validates a token using a secret key.</p>
     *
     * @param token  OTP string to validate
     * @param secret Shared secret
     * @return
     */
    public boolean validateSOTP(String token, AuthenticationSessionModel authSession) {
        System.out.println("VALIDATE SOTP");
        System.out.println("OTP Entered : " + token);
        System.out.println("OTP to be : " + authSession.getAuthNote(Details.AUTH_OTP));
        System.out.println("OTP expiry : " + authSession.getAuthNote(Details.OTP_EXPIRY));
        return false;
    }

    public void setCalendar(Calendar calendar) {
        this.clock.setCalendar(calendar);
    }

    private static class Clock {

        private final int interval;
        private Calendar calendar;

        public Clock(int interval) {
            this.interval = interval;
        }

        public long getCurrentInterval() {
            Calendar currentCalendar = this.calendar;

            if (currentCalendar == null) {
                currentCalendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            }

            return (currentCalendar.getTimeInMillis() / 1000) / this.interval;
        }

        public void setCalendar(Calendar calendar) {
            this.calendar = calendar;
        }
    }
}
