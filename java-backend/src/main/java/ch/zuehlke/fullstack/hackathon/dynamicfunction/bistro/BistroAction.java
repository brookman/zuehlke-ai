package ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.ChatMessageWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BistroAction implements Action {

    @Override
    public boolean canHandle(String actionName) {
        return actionName.equals("bistro_action");
    }

    @Override
    public ChatMessageWrapper execute(ChatFunctionCall functionCall) {
        String weekday = functionCall.getArguments().get("weekday").asText();
        boolean vegetarian = functionCall.getArguments().get("vegetarian").asBoolean();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("weekday", weekday);
        List<String> menusOfTheRequestedDay = getMenusByDay(weekday, vegetarian);
        response.put("food", menusOfTheRequestedDay.toString());

        return new ChatMessageWrapper(new ChatMessage(ChatMessageRole.FUNCTION.value(), response.toString(), "bistro_action"), menusOfTheRequestedDay.get(0));
    }

    @Override
    public ChatFunctionDynamic getFunction() {
        return ChatFunctionDynamic.builder()
                .name("bistro_action")
                .description("Determine for which day of the week the user wants to know the menu. If it is not monday to friday, return unknown. If the user wants to know data related to a specific day (like today), determine the week day on your own")
                .addProperty(ChatFunctionProperty.builder()
                        .name("weekday")
                        .type("string")
                        .description("The day of the week")
                        .enumValues(new HashSet<>(Arrays.stream(Weekday.values()).map(Weekday::name).toList()))
                        .required(true)
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("vegetarian")
                        .type("boolean")
                        .description("Either being true or false to decide if the vegetarian meal should be extracted or meat")
                        .required(true)
                        .build())
                .build();
    }

    private List<String> getMenusByDay(String weekday, boolean vegetarian) {
        List<MenuItem> weeklyMenu = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            weeklyMenu = mapper.readValue(new ClassPathResource("weekly_menu.json").getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weeklyMenu.stream()
                .filter(menu -> menu.getWeek_day().equals(weekday))
                .filter(menu -> !vegetarian || menu.getMenu_type().equals("Vegetarian"))
                .map(menu -> menu.getMenu_type() + ": " + menu.getName() + ";")
                .toList();
    }

    enum Weekday {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        UNKNOWN
    }
}
