package com.aidataquality.model.enums;

/**
 * Enumeration of data sensitivity levels
 */
public enum SensitivityLevel {
    PUBLIC,      // No sensitive data
    INTERNAL,    // Internal use only
    CONFIDENTIAL,// Confidential data
    RESTRICTED   // Highly sensitive (PII, PHI, financial)
}

