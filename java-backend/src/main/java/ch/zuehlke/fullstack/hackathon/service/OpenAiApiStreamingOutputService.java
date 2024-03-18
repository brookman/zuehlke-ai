package ch.zuehlke.fullstack.hackathon.service;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;

public class OpenAiApiStreamingOutputService {

    private final OpenAiService openAiService;

    @Value("${app.openapi.key}")
    private String apiKey;

    protected OpenAiApiStreamingOutputService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    public OpenAiApiStreamingOutputService() {
        this.openAiService = new OpenAiService(apiKey);
    }

    public Flowable<ChatMessage> streamChatCompletions(ChatCompletionRequest request) {
        var flowable = openAiService.streamChatCompletion(request);
        if (flowable == null) {
            throw new IllegalStateException("Flowable was null, indicating a problem with service setup.");
        }
        return flowable
                .map(ChatCompletionChunk::getChoices)
                .filter(choices -> !choices.isEmpty())
                .map(choices -> choices.get(0))
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent)
                .map(content -> new ChatMessage(ChatMessageRole.ASSISTANT.value(), content));
    }
}
