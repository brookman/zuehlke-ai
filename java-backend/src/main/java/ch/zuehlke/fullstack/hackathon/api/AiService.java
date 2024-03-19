package ch.zuehlke.fullstack.hackathon.api;

import ch.zuehlke.fullstack.hackathon.api.light.LightSwitch;
import ch.zuehlke.fullstack.hackathon.websocket.WebsocketMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
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

    public Flowable<WebsocketMessage> submitStreamed(String input) {
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

    public Optional<String> functionDynamic(String input) {
        ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), input);
        List<ChatMessage> messages = List.of(message);

        var responseMessage = executeCall(messages);

        ChatFunctionCall functionCall = responseMessage.getFunctionCall();
        if (functionCall != null) {
            if (functionCall.getName().equals("light_action")) {
                String status = functionCall.getArguments().get("light").asText();
                JsonNode jsonNode = lightAction(status);
                ChatMessage lightActionMessage = new ChatMessage(ChatMessageRole.FUNCTION.value(), jsonNode.toString(), "light_action");
                messages.add(lightActionMessage);
            } else if (functionCall.getName().equals("bistro_action")) {
                String weekday = functionCall.getArguments().get("weekday").asText();
                JsonNode jsonNode = getBistroAction(weekday);
                ChatMessage bistroMessage = new ChatMessage(ChatMessageRole.FUNCTION.value(), jsonNode.toString(), "bistro_action");
                messages.add(bistroMessage);
            }
        }

        var finalMessage = executeCall(messages);

        return Optional.ofNullable(finalMessage.getContent());
    }

    private static JsonNode lightAction(String action) {
        LightSwitch lightSwitch = LightSwitch.getInstance();
        if (action.equals("activate")) {
            lightSwitch.setStatus(true);
        } else {
            lightSwitch.setStatus(false);
        }
        log.info("LightSwitch is now " + lightSwitch.isOn());
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("status", "true");
        return response;
    }

    private static JsonNode getBistroAction(String weekday) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("weekday", weekday);
        response.put("food", "Pizza von Dieci");
        return response;
    }

    private ChatMessage executeCall(List<ChatMessage> messages) {
        var function = ChatFunctionDynamicInitiator.getFunction();

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-3.5-turbo-0613")
                .messages(messages)
                .functions(Collections.singletonList(function))
                .functionCall(ChatCompletionRequest.ChatCompletionRequestFunctionCall.of("auto"))
                .n(1)
                .maxTokens(100)
                .logitBias(new HashMap<>())
                .build();

        return getOpenAiService().createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
    }

}
