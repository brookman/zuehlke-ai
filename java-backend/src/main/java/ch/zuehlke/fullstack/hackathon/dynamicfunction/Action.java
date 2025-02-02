package ch.zuehlke.fullstack.hackathon.dynamicfunction;

import com.theokanning.openai.completion.chat.ChatFunctionCall;
import com.theokanning.openai.completion.chat.ChatFunctionDynamic;
import com.theokanning.openai.completion.chat.ChatMessage;


public interface Action {

    boolean canHandle(String actionName);
    ChatMessageWrapper execute(ChatFunctionCall functionCall);
    ChatFunctionDynamic getFunction();

}
