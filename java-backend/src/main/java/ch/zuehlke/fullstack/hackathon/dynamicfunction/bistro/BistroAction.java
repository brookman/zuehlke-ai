package ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.util.Arrays;
import java.util.HashSet;

public class BistroAction implements Action {

    @Override
    public ChatMessage execute(ChatFunctionCall functionCall) {
        String weekday = functionCall.getArguments().get("weekday").asText();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("weekday", weekday);
        response.put("food", "Pizza von Dieci"); //TODO read data from DB(?) by weekday
        return new ChatMessage(ChatMessageRole.FUNCTION.value(), response.toString(), "bistro_action");
    }

    @Override
    public ChatFunctionDynamic getFunction() {
        return ChatFunctionDynamic.builder()
                .name("bistro_action")
                .description("Determine for which day of the week the user wants to know the menu. If it is not monday to friday, return unknown")
                .addProperty(ChatFunctionProperty.builder()
                        .name("weekday")
                        .type("string")
                        .description("The day of the week")
                        .enumValues(new HashSet<>(Arrays.asList("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "UNKNOWN")))
                        .required(true)
                        .build())
                .build();
    }
}
