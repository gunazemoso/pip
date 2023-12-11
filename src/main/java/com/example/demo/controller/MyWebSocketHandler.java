package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MyWebSocketHandler extends TextWebSocketHandler {

    private static final String FILE_NAME = "/home/gunsvb/Desktop/sonar.txt";
    private String previousData = "";
    private boolean isFirstMessage = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private void checkForFileChanges(WebSocketSession session) {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                String newData = readNewDataFromFile();
                if (newData != null) {
                    session.sendMessage(new TextMessage(newData));
                }
            } catch (IOException e) {
                System.err.println("Error reading file: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private String readNewDataFromFile() throws IOException {
        StringBuilder newContent = new StringBuilder();
        AtomicBoolean isFileUpdated = new AtomicBoolean(false);

        try (Stream<String> fileStream = Files.lines(Path.of(FILE_NAME))) {
            fileStream.forEach(line -> {
                if (!line.equals(previousData)) {
                    newContent.append(line).append(System.lineSeparator());
                    previousData = line;
                    isFileUpdated.set(true);
                }
            });
        } catch (NoSuchFileException e) {
            System.err.println("File not found: " + FILE_NAME);
        }

        return isFileUpdated.get() ? newContent.toString() : null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Received message: " + message.getPayload());
        if (isFirstMessage) {
            isFirstMessage = false;
            checkForFileChanges(session);
        }
    }
}
