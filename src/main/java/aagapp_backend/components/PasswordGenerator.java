package aagapp_backend.components;

import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String ALPHANUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHANUMERIC_STRING.length());
            password.append(ALPHANUMERIC_STRING.charAt(index));
        }

        return password.toString();
    }
}
