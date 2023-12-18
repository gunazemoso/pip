package com.example.demo.exceptions;

public class ThreadInterruptedException extends WebSocketException {
    public ThreadInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}