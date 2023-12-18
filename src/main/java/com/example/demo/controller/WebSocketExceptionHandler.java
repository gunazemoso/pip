package com.example.demo.controller;

import com.example.demo.exceptions.FileIOException;
import com.example.demo.exceptions.ThreadInterruptedException;
import com.example.demo.exceptions.UnexpectedException;
import com.example.demo.exceptions.WebSocketMessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class WebSocketExceptionHandler {

    @ExceptionHandler(FileIOException.class)
    public ResponseEntity<String> handleFileIOException(FileIOException ex) {
        return new ResponseEntity<>("File IO Exception: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(WebSocketMessagingException.class)
    public ResponseEntity<String> handleWebSocketMessagingException(WebSocketMessagingException ex) {
        return new ResponseEntity<>("WebSocket Messaging Exception: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ThreadInterruptedException.class)
    public ResponseEntity<String> handleThreadInterruptedException(ThreadInterruptedException ex) {
        return new ResponseEntity<>("Thread Interrupted Exception: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<String> handleUnexpectedException(UnexpectedException ex) {
        return new ResponseEntity<>("Unexpected Exception: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
