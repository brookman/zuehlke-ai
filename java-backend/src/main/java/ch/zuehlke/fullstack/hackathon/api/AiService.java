package ch.zuehlke.fullstack.hackathon.api;

import ch.zuehlke.fullstack.hackathon.api.model.Prompt;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.ChatMessageWrapper;
import ch.zuehlke.fullstack.hackathon.websocket.WebsocketMessage;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AiService {

    private static final String GPT_MODEL = "gpt-3.5-turbo";

    private final ActionFactory actionFactory;
    private final OpenAiService openAiService;

    public AiService(ActionFactory actionFactory, @Value("${app.openapi.key}") String apiKey) {
        this.actionFactory = actionFactory;
        openAiService = new OpenAiService(apiKey);
    }

    public Optional<String> getImageByPrompt(String prompt) {
        if (prompt == null) {
            return Optional.empty();
        }

        CreateImageRequest request = CreateImageRequest.builder()
                .prompt("Create a photorealistic image of " + prompt)
                .size("256x256")
                .responseFormat("url")
                .n(1)
                .build();
        log.info("generating image...");

        return openAiService.createImage(request).getData().stream()
                .findFirst()
                .map(Image::getUrl);
    }

    private void checkFlowableNotNull(Flowable<ChatCompletionChunk> flowable) {
        if (flowable == null) {
            throw new IllegalStateException("Flowable was null, indicating a problem with service setup.");
        }
    }

    public Flowable<WebsocketMessage> submitFunctionStreamed(String input) {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), input);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(message);

        var request = createFunctionRequest(messages);
        var responseMessage = executeCall(request);

        Prompt prompt = new Prompt();

        ChatFunctionCall functionCall = responseMessage.getFunctionCall();
        if (functionCall != null) {
            actionFactory.getAction(functionCall.getName()).ifPresent(action -> {
                ChatMessageWrapper actionMessage = action.execute(functionCall);
                messages.add(actionMessage.chatMessage());
                prompt.setValue(actionMessage.imagePrompt());
            });
        }

        var chatRequest = createFunctionRequest(messages);
        var flowable = openAiService.streamChatCompletion(chatRequest);
        checkFlowableNotNull(flowable);

        return createWebsocketMessageFlowable(flowable, prompt.getValue());
    }

    private Flowable<WebsocketMessage> createWebsocketMessageFlowable(Flowable<ChatCompletionChunk> flowable, String prompt) {
        return Flowable.create(emitter -> {
            flowable.map(ChatCompletionChunk::getChoices)
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0))
                    .subscribe(choice -> {
                        var m = choice.getMessage();
                        if (m == null) {
                            emitter.onNext(new WebsocketMessage(null, null, true, false));
                            getImageByPrompt(prompt).ifPresent(s -> emitter.onNext(new WebsocketMessage(null, s, false, true)));
                            emitter.onComplete();
                            return;
                        }
                        var c = m.getContent();
                        if (c == null) {
                            emitter.onNext(new WebsocketMessage(null, null, true, false));
                            getImageByPrompt(prompt).ifPresent(s -> emitter.onNext(new WebsocketMessage(null, s, false, true)));
                            emitter.onComplete();
                            return;
                        }
                        emitter.onNext(new WebsocketMessage(c, null, false, false));


                    });
        }, BackpressureStrategy.BUFFER);
    }

    private ChatMessage executeCall(ChatCompletionRequest request) {
        return openAiService.createChatCompletion(request).getChoices().get(0).getMessage();
    }

    private ChatCompletionRequest createFunctionRequest(List<ChatMessage> messages) {
        List<ChatFunctionDynamic> functions = actionFactory.allowedActions.stream().map(Action::getFunction).toList();
        return ChatCompletionRequest
                .builder()
                .model(GPT_MODEL)
                .messages(messages)
                .functions(functions)
                .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                .n(1)
                .maxTokens(256)
                .logitBias(new HashMap<>())
                .build();
    }

}
