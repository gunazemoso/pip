package com.example.demo.exceptions;

public class UnexpectedException extends WebSocketException {
    public UnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}