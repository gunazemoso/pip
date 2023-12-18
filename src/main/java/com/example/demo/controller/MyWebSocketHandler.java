package com.example.demo.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final Path FILE_PATH = Paths.get("/home/gunsvb/Desktop/sonar.txt");
    private long lastFilePosition = 0;
    private final Object lock = new Object();
    private Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    public MyWebSocketHandler() {
        try {
            // Register directory with WatchService for file modification events
            Path parentDirectory = FILE_PATH.getParent();
            WatchService watchService = FileSystems.getDefault().newWatchService();
            parentDirectory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // Start a separate thread for handling file modification events
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                notifyClientsAboutFileChange();
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void notifyClientsAboutFileChange() {
        String newLines = readNewLines();
        if (!newLines.isEmpty()) {
            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    try {
                        session.sendMessage(new TextMessage(newLines));
                    } catch (IOException e) {
                        handleWebSocketException(session, "Error sending message: " + e.getMessage());
                    }
                }
            }
        }
    }

    private String readNewLines() {
        StringBuilder newLines = new StringBuilder();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_PATH.toFile(), "r")) {
            synchronized (lock) {
                randomAccessFile.seek(lastFilePosition);

                String line;
                while ((line = randomAccessFile.readLine()) != null) {
                    newLines.append(line).append("\n");
                }
                lastFilePosition = randomAccessFile.getFilePointer();
            }
        } catch (IOException e) {
            handleFileIOException(e);
        }
        return newLines.toString().trim();
    }

    private void handleFileIOException(IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
    }

    private void handleWebSocketException(WebSocketSession session, String errorMessage) {
        try {
            String errorEvent = String.format("{\"event\":\"error\",\"message\":\"%s\"}", errorMessage);
            session.sendMessage(new TextMessage(errorEvent));
        } catch (IOException e) {
            System.err.println("Error sending WebSocket error event: " + e.getMessage());
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload());
        sessions.add(session);
        sendInitialFileContent(session);
    }
    private void sendInitialFileContent(WebSocketSession session) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_PATH.toFile(), "r")) {
            StringBuilder initialContent = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                String line = randomAccessFile.readLine();
                if (line == null) {
                    break;  // Break if we reach the end of the file
                }
                initialContent.append(line).append("\n");
            }
            session.sendMessage(new TextMessage(initialContent.toString()));
        } catch (IOException e) {
            handleFileIOException(e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        synchronized (sessions) {
            sessions.remove(session);
        }
        System.out.println("Connection closed: " + session.getId());
    }
}
