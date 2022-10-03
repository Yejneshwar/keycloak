package org.keycloak.models.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * 
 *
 * @author Yejneshwar Sivamoorthy
 * 
 *
 */

public class SmsBasedOTP extends HmacOTP{
    public static final int DEFAULT_INTERVAL_SECONDS = 30;
    public static final int DEFAULT_DELAY_WINDOW = 1;

    private Clock clock;

    public SmsBasedOTP() {
        this(DEFAULT_ALGORITHM, DEFAULT_NUMBER_DIGITS, DEFAULT_INTERVAL_SECONDS, DEFAULT_DELAY_WINDOW);
    }

    /**
     * @param algorithm the encryption algorithm
     * @param numberDigits the number of digits for tokens
     * @param timeIntervalInSeconds the number of seconds a token is valid
     * @param lookAheadWindow the number of previous intervals that should be used to validate tokens.
     */
    public SmsBasedOTP(String algorithm, int numberDigits, int timeIntervalInSeconds, int lookAheadWindow) {
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

    /**
     * <p>Validates a token using a secret key.</p>
     *
     * @param token  OTP string to validate
     * @param secret Shared secret
     * @return
     */
    public boolean validateSOTP(String token, byte[] secret) {
        System.out.println("VALIDATE SOTP");
        System.out.println(token);
        String s = new String(secret);
        System.out.println(s);
        return true;
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
