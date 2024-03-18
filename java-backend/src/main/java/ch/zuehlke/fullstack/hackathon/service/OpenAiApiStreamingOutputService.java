package ch.zuehlke.fullstack.hackathon.service;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class OpenAiApiStreamingOutputService {

    private final OpenAiService openAiService;

    public OpenAiApiStreamingOutputService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }
    public List<ChatMessage> streamChatCompletions(ChatCompletionRequest request) {
        List<ChatMessage> accumulatedMessages = new ArrayList<>();
        // Invoke the streaming API
        Flowable<ChatCompletionChunk> flowable = openAiService.streamChatCompletion(request);
        if (flowable == null) {
            throw new IllegalStateException("Flowable was null, indicating a problem with service setup.");
        }
        // Process each chunk as it arrives
        flowable.subscribe(chunk -> {
            // Here, you'd process the chunk, potentially converting it to ChatMessage instances and accumulating them
            // This is a simplification. You'll need to adapt it based on actual logic for processing chunks.
            List<ChatCompletionChoice> choices = chunk.getChoices();
            if (!choices.isEmpty()) {
                // Extract the text from the first choice for simplicity. Adjust as needed.
                String content = choices.get(0).getMessage().getContent(); // Adjust this line based on the actual method to get text from ChatCompletionChoice
                ChatMessage message = new ChatMessage(ChatMessageRole.ASSISTANT.value(), content);
                accumulatedMessages.add(message);
            }
        });

        return accumulatedMessages;
    }
}
