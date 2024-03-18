package ch.zuehlke.fullstack.hackathon.service;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.Flowable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class OpenAiApiStreamingOutputServiceTest {

    @Mock
    private OpenAiService mockService;

    // Initialize mocks before each test
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldStreamOutputResponse_whenUserHasSubmittedValidTextInput() {
        // GIVEN
        String sampleResponseContent = "Hello, World!";
        Flowable<ChatCompletionChunk> mockFlowable = createMockFlowableForChatChunks(sampleResponseContent);
        given(mockService.streamChatCompletion(any())).willReturn(mockFlowable);
        // Use precise argument matching, reflecting how streamChatCompletions constructs its request
        ChatCompletionRequest expectedRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .build();
        given(mockService.streamChatCompletion(eq(expectedRequest))).willReturn(mockFlowable);

        var openAiApiStreamingOutputService = new OpenAiApiStreamingOutputService(mockService);

        // WHEN
        // Call the service method that should internally subscribe to the Flowable and process it
        List<ChatMessage> resultMessages = openAiApiStreamingOutputService.streamChatCompletions(expectedRequest);

        // THEN
        verify(mockService).streamChatCompletion(any(ChatCompletionRequest.class));
        assertEquals(1, resultMessages.size(), "Should have one message");
        assertEquals(sampleResponseContent, resultMessages.get(0).getContent(), "Message content should match");    }

    private Flowable<ChatCompletionChunk> createMockFlowableForChatChunks(String responseContent) {
        ChatCompletionChunk chunk = new ChatCompletionChunk();
        chunk.setId("sampleId"); // Set a sample or mock ID
        chunk.setObject("chat.completion.chunk"); // Set the object type
        chunk.setCreated(System.currentTimeMillis() / 1000); // Set the creation time
        chunk.setModel("gpt-3.5-turbo"); // Set the model used

        // Assuming ChatCompletionChoice has a constructor or setters to set its properties
        ChatCompletionChoice choice = new ChatCompletionChoice();
        // Here, set the choice properties, such as the text of the completion.
        // Assuming it has a setText method or similar. Adjust according to the actual class structure.
        choice.setMessage(new ChatMessage(ChatMessageRole.USER.value(), responseContent));

        List<ChatCompletionChoice> choices = Collections.singletonList(choice);
        chunk.setChoices(choices); // Set the list of choices

        return Flowable.just(chunk);
    }

}
