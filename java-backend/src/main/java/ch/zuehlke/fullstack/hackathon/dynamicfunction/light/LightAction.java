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

import java.util.Arrays;
import java.util.HashSet;

@Slf4j
public class LightAction implements Action {

    @Override
    public boolean canHandle(String actionName) {
        return actionName.equals("light_action");
    }

    @Override
    public ChatMessageWrapper execute(ChatFunctionCall functionCall) {
        boolean activate = functionCall.getArguments().get("light").asBoolean();

        LightSwitch lightSwitch = LightSwitch.getInstance();
        if (activate) {
            lightSwitch.setStatus(true);
            log.info("LightSwitch is now activated");
        } else {
            lightSwitch.setStatus(false);
            log.info("LightSwitch is now deactivated");
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode response = mapper.createObjectNode();
        response.put("status", true);

        return new ChatMessageWrapper(new  ChatMessage(ChatMessageRole.FUNCTION.value(), response.toString(), "light_action"), null);
    }

    @Override
    public ChatFunctionDynamic getFunction() {
        return ChatFunctionDynamic.builder()
                .name("light_action")
                .description("Determine if the light needs to be activated or deactivated")
                .addProperty(ChatFunctionProperty.builder()
                        .name("light")
                        .type("boolean")
                        .description("The value to either activate or deactivate")
                        .required(true)
                        .build())
                .build();
    }
}
