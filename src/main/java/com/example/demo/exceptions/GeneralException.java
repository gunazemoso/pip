package com.example.demo.exceptions;

public class GeneralException extends CustomWebSocketException {
    public GeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
