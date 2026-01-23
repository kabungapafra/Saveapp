package com.example.save.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.Random;

/**
 * Utility class for input validation
 */
public class ValidationUtils {

    /**
     * Validates if the email is in correct format
     * 
     * @param email Email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validates phone number (Uganda format: 9-10 digits)
     * 
     * @param phone Phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        }
        // Remove spaces and check if it's 9 or 10 digits
        String cleanPhone = normalizePhone(phone);
        return cleanPhone.matches("^[7][0-9]{8}$"); // Uganda format starting with 7
    }

    /**
     * Normalizes phone number by removing spaces and leading zero
     * 
     * @param phone Phone number to normalize
     * @return Normalized phone number (e.g. 772123456)
     */
    public static String normalizePhone(String phone) {
        if (phone == null)
            return "";
        String clean = phone.replaceAll("\\s+", "");
        if (clean.startsWith("+256")) {
            clean = clean.substring(4);
        }
        if (clean.startsWith("256")) {
            clean = clean.substring(3);
        }
        if (clean.startsWith("0")) {
            clean = clean.substring(1);
        }
        return clean;
    }

    /**
     * Checks if input is not empty
     * 
     * @param input String to check
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String input) {
        return !TextUtils.isEmpty(input) && !input.trim().isEmpty();
    }

    /**
     * Shows error message on EditText
     * 
     * @param field   EditText to show error on
     * @param message Error message
     */
    public static void showError(EditText field, String message) {
        if (field != null) {
            field.setError(message);
            field.requestFocus();
        }
    }

    /**
     * Generates a random 6-digit OTP
     * 
     * @return String containing 6-digit OTP
     */
    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Range: 100000-999999
        return String.valueOf(otp);
    }

    /**
     * Validates password strength (minimum 8 characters)
     * 
     * @param password Password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 8;
    }
}
