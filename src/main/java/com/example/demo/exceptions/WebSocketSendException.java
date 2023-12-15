package com.example.demo.exceptions;

public class WebSocketSendException extends CustomWebSocketException {
    public WebSocketSendException(String message, Throwable cause) {
        super(message, cause);
    }
}