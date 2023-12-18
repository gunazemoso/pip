package com.example.demo.exceptions;

public class WebSocketMessagingException extends WebSocketException {
    public WebSocketMessagingException(String message, Throwable cause) {
        super(message, cause);
    }
}