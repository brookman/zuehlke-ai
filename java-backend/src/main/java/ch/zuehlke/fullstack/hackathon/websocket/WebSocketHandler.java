package ch.zuehlke.fullstack.hackathon.websocket;

import ch.zuehlke.fullstack.hackathon.api.AiService;
import ch.zuehlke.fullstack.hackathon.model.MessageDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final AiService aiService;

    private final AtomicInteger messageIds = new AtomicInteger();

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws IOException {
        var messageId = messageIds.getAndIncrement();
        try {
            this.aiService.submitFunctionStreamed(message.getPayload())
                    .subscribe(websocketMessage -> session.sendMessage(serialize(buildPayload(messageId, websocketMessage))));
        } catch (Exception exception) {
            String errorMessage = "The message " + message.getPayload() + " could not be processed.";
            log.error(errorMessage, exception);
            session.sendMessage(serialize(new MessageDto.Error(errorMessage)));
        }
    }

    private MessageDto buildPayload(int messageId, WebsocketMessage websocketMessage) {
        if (websocketMessage.isImageMessage()) {
            String imgUrl = websocketMessage.imgUrl();
            var response = new MessageDto.AddImageToMessage(messageId, imgUrl);
            log.info(response.toString());
            return response;
        }
        if (websocketMessage.endMessage()) {
            var response = new MessageDto.ChatMessageFinished(messageId);
            log.info(response.toString());
            return response;
        }
        var response = new MessageDto.ChatMessageChunk(messageId, websocketMessage.content());
        log.info(response.toString());
        return response;
    }

    private TextMessage serialize(MessageDto dto) {
        try {
            return new TextMessage(new ObjectMapper().writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while serializing DTO: " + e.getMessage());
        }
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