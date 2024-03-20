package ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.ChatMessageWrapper;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.model.MenuItem;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.model.RelativeDay;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.model.Weekday;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class BistroAction implements Action {

    private static final String ACTION = "bistro_action";
    private static final String WEEKDAY = "weekday";
    private static final String VEGETARIAN = "vegetarian";
    private static final String RELATIVE_DAY = "relative_day";
    private static final String MENU_FILE = "weekly_menu.json";

    @Override
    public boolean canHandle(String actionName) {
        return actionName.equals(ACTION);
    }

    @Override
    public ChatMessageWrapper execute(ChatFunctionCall functionCall) {
        String weekday = functionCall.getArguments().get(WEEKDAY).asText();
        boolean vegetarian = functionCall.getArguments().get(VEGETARIAN).asBoolean();
        JsonNode relativeDayJsonNode = functionCall.getArguments().get(RELATIVE_DAY);
        String relativeDay = relativeDayJsonNode == null ? null : relativeDayJsonNode.asText();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();

        if (relativeDay != null) {
            log.info("Get relative date");
            LocalDate date = LocalDate.now();
            int dayOfWeek = date.getDayOfWeek().getValue();
            if (relativeDay.equals(RelativeDay.YESTERDAY.name())) {
                dayOfWeek--;
            } else if(relativeDay.equals(RelativeDay.TOMORROW.name())) {
                dayOfWeek++;
            }
            weekday = Weekday.getWeekday(dayOfWeek).toString();
            log.info("Got relative date: " + weekday);

        }
        response.put(WEEKDAY, weekday);
        List<String> menusOfTheRequestedDay = getMenusByDay(weekday, vegetarian);
        response.put("food", menusOfTheRequestedDay.toString());

        return new ChatMessageWrapper(new ChatMessage(ChatMessageRole.FUNCTION.value(), response.toString(), ACTION), menusOfTheRequestedDay.get(0));
    }

    @Override
    public ChatFunctionDynamic getFunction() {
        return ChatFunctionDynamic.builder()
                .name(ACTION)
                .description("Determine for which day of the week the user wants to know the menu. If it is not monday to friday, return unknown. If the user wants to know data related to a specific day (like today), determine the week day on your own")
                .addProperty(ChatFunctionProperty.builder()
                        .name(WEEKDAY)
                        .type("string")
                        .description("The day of the week")
                        .enumValues(new HashSet<>(Arrays.stream(Weekday.values()).map(Weekday::name).toList()))
                        .required(true)
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name(VEGETARIAN)
                        .type("boolean")
                        .description("Either being true or false to decide if the vegetarian meal should be extracted or meat")
                        .required(true)
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name(RELATIVE_DAY)
                        .type("string")
                        .description("The relative day like today, tomorrow, yesterday")
                        .enumValues(new HashSet<>(Arrays.stream(RelativeDay.values()).map(RelativeDay::name).toList()))
                        .required(false)
                        .build())
                .build();
    }

    private List<String> getMenusByDay(String weekday, boolean vegetarian) {
        List<MenuItem> weeklyMenu = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            weeklyMenu = mapper.readValue(new ClassPathResource(MENU_FILE).getInputStream(), new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weeklyMenu.stream()
                .filter(menu -> menu.getWeek_day().equals(weekday))
                .filter(menu -> !vegetarian || menu.getMenu_type().equals("Vegetarian"))
                .map(menu -> menu.getMenu_type() + ": " + menu.getName() + ";")
                .toList();
    }
}
