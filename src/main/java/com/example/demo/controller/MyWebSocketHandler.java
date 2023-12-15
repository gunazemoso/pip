package com.example.demo.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final String FILE_NAME = "/home/gunsvb/Desktop/sonar.txt";
    private boolean isFirstMessage = true;
    private List<String> fileLines;
    private long lastModifiedTime;

    private void sendFileContent(WebSocketSession session) {
        try {
            String fileContent = String.join("\n", Files.readAllLines(Paths.get(FILE_NAME)));
            session.sendMessage(new TextMessage(fileContent));
        } catch (IOException e) {
            System.err.println("Error sending file content: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        isFirstMessage = true;
        // Perform cleanup or additional logic when a connection is closed
        System.out.println("Connection closed: " + session.getId());
    }

    private void checkForFileChanges(WebSocketSession session) {
        try {
            if (isFirstMessage) {
                fileLines = Files.readAllLines(Paths.get(FILE_NAME));
                sendFileContent(session);
                lastModifiedTime = Files.getLastModifiedTime(Paths.get(FILE_NAME)).toMillis();
                isFirstMessage = false;
            }

            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                try {
                    long currentModifiedTime = Files.getLastModifiedTime(Paths.get(FILE_NAME)).toMillis();
                    if (currentModifiedTime > lastModifiedTime) {
                        List<String> newLines = getNewLines();
                        if (!newLines.isEmpty()) {
                            String newData = String.join("\n", newLines);
                            session.sendMessage(new TextMessage(newData));
                        }
                        lastModifiedTime = currentModifiedTime;
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                }
            }, 0, 1, TimeUnit.SECONDS);

        } catch (IOException e) {
            System.err.println("Error initializing file: " + e.getMessage());
        }
    }

    private List<String> getNewLines() {
        try {
            List<String> currentLines = Files.readAllLines(Paths.get(FILE_NAME));

            int startIndex = 0;
            if (fileLines.size() < currentLines.size()) {
                startIndex = fileLines.size();
            }

            List<String> newLines = currentLines.subList(startIndex, currentLines.size());
            fileLines = currentLines;
            return newLines;
        } catch (IOException e) {
            System.err.println("Error reading file lines: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload());
        checkForFileChanges(session);
    }
}