package com.logstream.logstreambackend;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class LogWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions =
            new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) throws Exception {

        sessions.add(session);

        System.out.println(
                "WebSocket client connected: "
                        + session.getId()
        );

        session.sendMessage(
                new TextMessage(
                        "Connected to Log Stream Live Tail"
                )
        );
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws Exception {

        System.out.println(
                "Received WebSocket message: "
                        + message.getPayload()
        );
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            org.springframework.web.socket.CloseStatus status)
            throws Exception {

        sessions.remove(session);

        System.out.println(
                "WebSocket client disconnected: "
                        + session.getId()
        );
    }

    public static void broadcastLog(String logMessage) {

        for (WebSocketSession session : sessions) {

            if (session.isOpen()) {

                try {
                    session.sendMessage(
                            new TextMessage(logMessage)
                    );

                } catch (Exception e) {

                    System.err.println(
                            "Failed to send log to WebSocket: "
                                    + e.getMessage()
                    );
                }
            }
        }
    }
}