package com.example.demo.controller;

import com.example.demo.exceptions.CustomWebSocketException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final String FILE_NAME = "/home/gunsvb/Desktop/sonar.txt"; // Use a shared location accessible by all processes
    private final Object lock = new Object(); // Object for synchronization

    private boolean isFirstMessage(WebSocketSession session) {
        Object attribute = session.getAttributes().get("isFirstMessage");
        return attribute == null || (boolean) attribute;
    }

    private void setNotFirstMessage(WebSocketSession session) {
        session.getAttributes().put("isFirstMessage", false);
    }

    private void sendFileContent(WebSocketSession session) {
        try {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(FILE_NAME)));
            session.sendMessage(new TextMessage(fileContent));
        } catch (IOException e) {
            System.err.println("Error sending file content: " + e.getMessage());
        }
    }

    private void checkForFileChanges(WebSocketSession session) {
        try {
            synchronized (lock) {
                if (isFirstMessage(session)) {
                    setFileLines(session, Files.readAllLines(Paths.get(FILE_NAME)));
                    sendFileContent(session);
                    setNotFirstMessage(session);
                }

                Thread thread = new Thread(() -> {

                    while (true) {
                        try {
                            long currentModifiedTime = Files.getLastModifiedTime(Paths.get(FILE_NAME)).toMillis();
                            if (currentModifiedTime > getLastModifiedTime(session)) {
                                List<String> newLines = getNewLines(session, getFileLines(session));
                                if (!newLines.isEmpty()) {
                                    String newData = String.join("\n", newLines);
                                    try {
                                        session.sendMessage(new TextMessage(newData));
                                    } catch (IOException e) {
                                        System.err.println("Error sending message: " + e.getMessage());
                                    }
                                }
                                setLastModifiedTime(session, currentModifiedTime);
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + e.getMessage());
                        }

                        try {
                            Thread.sleep(1000); // Adjust the sleep duration as needed
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                });

                thread.start();
            }
        } catch (Exception e) {
            System.err.println("Error initializing file: " + e.getMessage());
        }
    }

    private long getLastModifiedTime(WebSocketSession session) {
        Object attribute = session.getAttributes().get("lastModifiedTime");
        return attribute == null ? 0L : (long) attribute;
    }

    private void setLastModifiedTime(WebSocketSession session, long value) {
        session.getAttributes().put("lastModifiedTime", value);
    }


    private List<String> getNewLines(WebSocketSession session, List<String> fileLines) {
        try {
            List<String> currentLines = Files.readAllLines(Paths.get(FILE_NAME));
            int startIndex = 0;
            if (fileLines.size() < currentLines.size()) {
                startIndex = fileLines.size();
            }

            List<String> newLines = currentLines.subList(startIndex, currentLines.size());
            fileLines = currentLines;
            setFileLines(session, fileLines);
            return newLines;
        } catch (IOException e) {
            System.err.println("Error reading file lines: " + e.getMessage());
            return List.of();
        }
    }

    private List<String> getFileLines(WebSocketSession session) {
        Object attribute = session.getAttributes().get("fileLines");
        return attribute == null ? List.of() : (List<String>) attribute;
    }

    private void setFileLines(WebSocketSession session, List<String> lines) {
        session.getAttributes().put("fileLines", lines);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        session.getAttributes().put("isFirstMessage", true); // Reset isFirstMessage on connection close
        // Perform cleanup or additional logic when a connection is closed
        System.out.println("Connection closed: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload());
        checkForFileChanges(session);
    }

    private void handleException(WebSocketSession session, CustomWebSocketException e) {
        // Log the exception details
        System.err.println("Exception occurred: " + e.getMessage());

        // Send an error message to the WebSocket session
        sendErrorMessage(session, "ERROR: " + e.getMessage());
    }
}
