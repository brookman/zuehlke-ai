package ch.zuehlke.fullstack.hackathon.websocket;

import ch.zuehlke.fullstack.hackathon.model.MessageOfTheDayDto;
import ch.zuehlke.fullstack.hackathon.model.SubmitResponseDto;
import ch.zuehlke.fullstack.hackathon.service.ExampleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    private final ExampleService exampleService;

    @Override
    public void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) throws IOException {

        SubmitResponseDto result = null;
        log.info(message.getPayload());
        try {
            result = this.exampleService.submit(message.getPayload());
        } catch (Exception exception) {
            String errorMessage = "Message of the day could not be fetched";
            log.error(errorMessage, exception);
            session.sendMessage(new TextMessage("\"" + errorMessage + "\""));
        }
        session.sendMessage(new TextMessage("\"" + result.content() + "\""));



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