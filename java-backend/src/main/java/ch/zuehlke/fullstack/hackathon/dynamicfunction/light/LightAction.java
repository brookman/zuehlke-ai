package ch.zuehlke.fullstack.hackathon.dynamicfunction.light;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.ChatMessageWrapper;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model.LightSwitch;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatFunctionProperty;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;

@Slf4j
public class LightAction implements Action {

    private static final String ACTION = "light_action";

    private final LightSwitch lightSwitch;

    public LightAction(LightSwitch lightSwitch) {
        this.lightSwitch = lightSwitch;
    }

    @Override
    public boolean canHandle(String actionName) {
        return actionName.equals(ACTION);
    }

    @Override
    public ChatMessageWrapper execute(ChatFunctionCall functionCall) {
        boolean activate = functionCall.getArguments().get("light").asBoolean();
        LightSwitch.Color color = LightSwitch.Color.valueOf(functionCall.getArguments().get("color").asText());
        log.info("Color: " + color.name());
        if (activate) {
            lightSwitch.setColor(color);
            log.info("LightSwitch is now activated");
        } else {
            lightSwitch.setColor(LightSwitch.Color.OFF);
            log.info("LightSwitch is now deactivated");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("status", true);

        return new ChatMessageWrapper(new ChatMessage(ChatMessageRole.FUNCTION.value(), response.toString(), ACTION), null);
    }

    @Override
    public ChatFunctionDynamic getFunction() {
        return ChatFunctionDynamic.builder()
                .name(ACTION)
                .description("Determine if the light needs to be activated or deactivated")
                .addProperty(ChatFunctionProperty.builder()
                        .name("light")
                        .type("boolean")
                        .description("The value to either activate or deactivate")
                        .required(true)
                        .build())
                .addProperty(ChatFunctionProperty.builder()
                        .name("color")
                        .type("string")
                        .description("The color of the light. If you dont know which color to pick, pick WHITE.")
                        .enumValues(new HashSet<>(List.of(LightSwitch.Color.values())))
                        .required(true)
                        .build())
                .build();
    }
}
