package com.aidataquality.exception;

/**
 * Exception thrown when file format is not supported
 */
public class UnsupportedFileFormatException extends RuntimeException {
    
    public UnsupportedFileFormatException(String message) {
        super(message);
    }
}

