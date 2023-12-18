package com.example.demo.exceptions;

public class WebSocketException extends RuntimeException {
    public WebSocketException(String message, Throwable cause) {
        super(message);
    }
}