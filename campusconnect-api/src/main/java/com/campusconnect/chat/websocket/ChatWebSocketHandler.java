package com.campusconnect.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatWebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);

        if (userId == null) {
            log.warn("WebSocket 连接缺少 userId，关闭连接");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        sessionManager.addSession(userId, session);

        session.sendMessage(new TextMessage("""
            {"type":"CONNECTED","message":"WebSocket连接成功"}
            """));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("收到 WebSocket 消息：{}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionManager.removeSession(session);
    }

    private Long getUserId(WebSocketSession session) {
        if (session.getUri() == null) {
            return null;
        }

        String userId = UriComponentsBuilder
                .fromUri(session.getUri())
                .build()
                .getQueryParams()
                .getFirst("userId");

        if (userId == null || userId.isBlank()) {
            return null;
        }

        return Long.valueOf(userId);
    }
}