package ch.zuehlke.fullstack.hackathon.dynamicfunction;

import com.theokanning.openai.completion.chat.ChatMessage;

public record ChatMessageWrapper(ChatMessage chatMessage, String imagePrompt) {
}
