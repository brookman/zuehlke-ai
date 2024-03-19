package ch.zuehlke.fullstack.hackathon.api;

import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ChatFunctionDynamicInitiator {

    public static List<ChatFunctionDynamic> getFunction() {
        ChatFunctionDynamic lightFunction = ChatFunctionDynamic.builder()
                .name("light_action")
                .description("Determine if the light needs to be activated or deactivated")
                .addProperty(ChatFunctionProperty.builder()
                        .name("light")
                        .type("string")
                        .description("The value to either activate or deactivate")
                        .enumValues(new HashSet<>(Arrays.asList("ACTIVATE", "DEACTIVATE")))
                        .required(true)
                        .build())
                .build();

        ChatFunctionDynamic bistroFunction = ChatFunctionDynamic.builder()
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

        return List.of(lightFunction, bistroFunction);
    }
}
