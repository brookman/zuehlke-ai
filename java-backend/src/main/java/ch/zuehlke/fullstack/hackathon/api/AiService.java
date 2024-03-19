package ch.zuehlke.fullstack.hackathon.api;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.BistroAction;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.LightAction;
import ch.zuehlke.fullstack.hackathon.websocket.WebsocketMessage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
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
    private static final List<Action> ALLOWED_ACTIONS = List.of(new LightAction(), new BistroAction());

    @Value("${app.openapi.key}")
    private String apiKey;

    private OpenAiService openAiService;

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

        ChatFunctionCall functionCall = responseMessage.getFunctionCall();
        if (functionCall != null) {
            if (functionCall.getName().equals("light_action")) {
                var lightAction = new LightAction();
                ChatMessage lightActionMessage = lightAction.execute(functionCall);
                messages.add(lightActionMessage);
            } else if (functionCall.getName().equals("bistro_action")) {
                var bistroAction = new BistroAction();
                ChatMessage bistroMessage = bistroAction.execute(functionCall);
                messages.add(bistroMessage);
            }
        }

        var chatRequest = createFunctionRequest(messages);
        var flowable = getOpenAiService().streamChatCompletion(chatRequest);
        checkFlowableNotNull(flowable);

        return createWebsocketMessageFlowable(flowable);
    }

    private Flowable<WebsocketMessage> createWebsocketMessageFlowable(Flowable<ChatCompletionChunk> flowable) {
        return Flowable.create(emitter -> {
            flowable.map(ChatCompletionChunk::getChoices)
                    .filter(choices -> !choices.isEmpty())
                    .map(choices -> choices.get(0))
                    .subscribe(choice -> {
                        var m = choice.getMessage();
                        if (m == null) {
                            emitter.onNext(new WebsocketMessage(null, true)); // Send end signal
                            emitter.onComplete();
                            return;
                        }
                        var c = m.getContent();
                        if (c == null) {
                            emitter.onNext(new WebsocketMessage(null, true)); // Send end signal
                            emitter.onComplete();
                            return;
                        }
                        emitter.onNext(new WebsocketMessage(c, false));


                    });
        }, BackpressureStrategy.BUFFER);
    }

    private ChatMessage executeCall(ChatCompletionRequest request) {
        return getOpenAiService().createChatCompletion(request).getChoices().get(0).getMessage();
    }

    private ChatCompletionRequest createFunctionRequest(List<ChatMessage> messages) {
        List<ChatFunctionDynamic> functions = ALLOWED_ACTIONS.stream().map(Action::getFunction).toList();
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
