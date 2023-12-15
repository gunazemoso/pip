package com.example.demo.exceptions;

public  class CustomWebSocketException extends Exception {
    public CustomWebSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}