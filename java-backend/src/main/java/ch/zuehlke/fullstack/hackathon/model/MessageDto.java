package ch.zuehlke.fullstack.hackathon.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MessageDto.ChatMessageChunk.class, name = "ChatMessageChunk"),
        @JsonSubTypes.Type(value = MessageDto.ChatMessageFinished.class, name = "ChatMessageFinished"),
        @JsonSubTypes.Type(value = MessageDto.AddImageToMessage.class, name = "AddImageToMessage"),
        @JsonSubTypes.Type(value = MessageDto.Error.class, name = "Error")
})
public sealed interface MessageDto permits MessageDto.ChatMessageChunk, MessageDto.ChatMessageFinished, MessageDto.AddImageToMessage, MessageDto.Error {

    @JsonTypeName("ChatMessageChunk")
    record ChatMessageChunk(int messageId, String chunk) implements MessageDto {
    }

    @JsonTypeName("ChatMessageFinished")
    record ChatMessageFinished(int messageId) implements MessageDto {
    }

    @JsonTypeName("AddImageToMessage")
    record AddImageToMessage(int messageId, String imageUrl) implements MessageDto {
    }

    @JsonTypeName("Error")
    record Error(String errorMessage) implements MessageDto {
    }
}


