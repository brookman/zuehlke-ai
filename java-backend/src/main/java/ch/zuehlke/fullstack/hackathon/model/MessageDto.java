package ch.zuehlke.fullstack.hackathon.model;

import java.util.Optional;

public sealed interface MessageDto permits MessageDto.ChatMessageChunk, MessageDto.ChatMessageFinished, MessageDto.Error {

    record ChatMessageChunk(int messageId, String chunk) implements MessageDto {
    }

    record ChatMessageFinished(int messageId, String imageUrl) implements MessageDto {
    }

    record Error(String errorMessage) implements MessageDto {
    }
}


