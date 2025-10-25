package com.aidataquality.exception;

/**
 * Custom exception for data quality related errors
 */
public class DataQualityException extends RuntimeException {
    
    public DataQualityException(String message) {
        super(message);
    }
    
    public DataQualityException(String message, Throwable cause) {
        super(message, cause);
    }
}

