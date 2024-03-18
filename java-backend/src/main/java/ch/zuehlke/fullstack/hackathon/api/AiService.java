package ch.zuehlke.fullstack.hackathon.api;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AiService {

    private final String SYSTEM_PROMPT = "Given a user request, respond with a JSON object specifying the 'action' to be taken. Available actions are 'turn_light_on', 'turn_light_off', and 'get_bistro_menu'. If requesting a bistro menu, also include the 'day'. Example response: {'action': 'get_bistro_menu', 'day': 'Monday'}. Values of your response are always in English. If the request does not match with any action, the action is UNKNOWN";

    @Value("${app.openapi.key}")
    private String apiKey;

    private OpenAiService openAiService;

    public Optional<String> getMessageOfTheDay() {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), "Write a message of the day for a software engineer.");
        List<ChatMessage> messages = List.of(message);
        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model("gpt-3.5-turbo")
                .maxTokens(100)
                .n(1)
                .build();

        return getOpenAiService().createChatCompletion(chatRequest).getChoices().stream()
                .findFirst()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent);
    }

    public Optional<String> getCatImageUrl() {
        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("Draw an image of a cat engineering software.")
                .size("512x512")
                .responseFormat("url")
                .n(1)
                .build();

        return getOpenAiService().createImage(request).getData().stream()
                .findFirst()
                .map(Image::getUrl);
    }

    private OpenAiService getOpenAiService() {
        if (openAiService == null) {
            this.openAiService = new OpenAiService(apiKey);
        }

        return openAiService;
    }

    public Optional<String> submit(String input) {
        ChatMessage promptFormat = new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT);
        ChatMessage initialResponse = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "Okay, from now on I will always respond in your requested format.");
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), input);
        List<ChatMessage> messages = List.of(
                promptFormat,
                initialResponse,
                message);

        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model("gpt-3.5-turbo")
                .maxTokens(100)
                .n(1)
                .build();

        return getOpenAiService().createChatCompletion(chatRequest).getChoices().stream()
                .findFirst()
                .map(ChatCompletionChoice::getMessage)
                .map(ChatMessage::getContent);
    }

    public Flowable<String> submitStreamed(String input) {
        ChatMessage promptFormat = new ChatMessage(ChatMessageRole.SYSTEM.value(), SYSTEM_PROMPT);
        ChatMessage initialResponse = new ChatMessage(ChatMessageRole.ASSISTANT.value(), "Okay, from now on I will always respond in your requested format.");
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), input);
        List<ChatMessage> messages = List.of(
                promptFormat,
                initialResponse,
                message);

        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
                .messages(messages)
                .model("gpt-3.5-turbo")
                .maxTokens(256)
                .n(1)
                .stream(true)
                .build();

        var flowable = getOpenAiService().streamChatCompletion(chatRequest);
        if (flowable == null) {
            throw new IllegalStateException("Flowable was null, indicating a problem with service setup.");
        }

        return Flowable.create(emitter -> {
            flowable.map(ChatCompletionChunk::getChoices)
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0))
                    .subscribe(choice -> {
                        var m = choice.getMessage();
                        if (m == null) {
                            emitter.onComplete();
                            return;
                        }
                        var c = m.getContent();
                        if (c == null) {
                            emitter.onComplete();
                            return;
                        }
                        emitter.onNext(c);


                    });
        }, BackpressureStrategy.BUFFER);
    }
}
