package com.example.save.utils;

public class DesignMode {
    /**
     * MASTER TOGGLE: Set to true to bypass all input validation
     * and allow free movement between screens for design purposes.
     */
    public static final boolean IS_DESIGN_MODE = true;

    /**
     * Check if validation should be bypassed
     */
    public static boolean shouldBypass() {
        return IS_DESIGN_MODE;
    }
}
