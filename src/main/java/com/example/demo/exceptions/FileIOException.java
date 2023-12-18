package com.example.demo.exceptions;

public class FileIOException extends WebSocketException {
    public FileIOException(String message, Throwable cause) {
        super(message, cause);
    }
}