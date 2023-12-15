package com.example.demo.exceptions;

public  class FileReadException extends CustomWebSocketException {
    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }
}

