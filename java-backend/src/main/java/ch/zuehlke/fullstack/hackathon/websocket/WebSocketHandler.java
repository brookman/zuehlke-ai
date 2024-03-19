package ch.zuehlke.fullstack.hackathon.websocket;

import ch.zuehlke.fullstack.hackathon.api.AiService;
import ch.zuehlke.fullstack.hackathon.service.ExampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final ExampleService exampleService;
    private final AiService aiService;

    private final AtomicInteger messageIds = new AtomicInteger();

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws IOException {
        var messageId = messageIds.getAndIncrement();
        try {

            this.aiService.submitStreamed(message.getPayload()).subscribe(websocketMessage -> {
                session.sendMessage(buildPayload(messageId, websocketMessage));
            });
        } catch (Exception exception) {
            String errorMessage = "Message of the day could not be fetched";
            log.error(errorMessage, exception);
            session.sendMessage(new TextMessage("\"" + errorMessage + "\""));
        }
    }

    private TextMessage buildPayload(int messageId, WebsocketMessage websocketMessage) {
        if (websocketMessage.endMessage()) {
            return new TextMessage("{ \"messageId\": " + messageId + ", \"chunk\": " + websocketMessage.content() + " }");
        }
        return new TextMessage("{ \"messageId\": " + messageId + ", \"chunk\": \"" + websocketMessage.content() + "\" }");
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
    }

    @Override
    protected void handleBinaryMessage(@NotNull WebSocketSession session, @NotNull BinaryMessage message) {
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {
    }
}