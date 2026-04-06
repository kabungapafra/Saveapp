package com.example.save.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.Random;

/**
 * Utility class for input validation
 * 
 * SECURITY NOTE: These are client-side validations for UX only.
 * ALL validations MUST be re-validated on the backend for security.
 * Client-side validation can be bypassed by malicious users.
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
        // Normalize first, then check full E.164 Uganda format
        String normalized = normalizePhone(phone);
        return normalized.matches("^\\+256[7][0-9]{8}$");
    }

    /**
     * Normalizes a Uganda phone number to full E.164 format: +256XXXXXXXXX
     * Accepts: +2567XXXXXXXX, 07XXXXXXXX, 7XXXXXXXX, +256 7XX XXX XXX, etc.
     *
     * @param phone Phone number to normalize
     * @return Normalized phone number in +256XXXXXXXXX format, or original if unrecognized
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return "";

        // Strip all spaces, dashes, parentheses
        String clean = phone.replaceAll("[\\s\\-()]", "");

        // Already full E.164 Uganda
        if (clean.matches("^\\+2567[0-9]{8}$")) {
            return clean;
        }
        // International without +
        if (clean.matches("^2567[0-9]{8}$")) {
            return "+" + clean;
        }
        // Local with leading 0 (e.g. 0772123456)
        if (clean.matches("^07[0-9]{8}$")) {
            return "+256" + clean.substring(1);
        }
        // Local without 0 (e.g. 772123456)
        if (clean.matches("^7[0-9]{8}$")) {
            return "+256" + clean;
        }

        // Unrecognized — return as-is so validation catches it
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
     * SECURITY WARNING: This is for testing/demo only.
     * OTP generation MUST be done on backend for security.
     * Backend should use cryptographically secure random number generation.
     * 
     * @return String containing 6-digit OTP
     */
    @Deprecated
    public static String generateOTP() {
        // SECURITY: This should not be used in production
        // Backend should generate OTP using secure random number generator
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
