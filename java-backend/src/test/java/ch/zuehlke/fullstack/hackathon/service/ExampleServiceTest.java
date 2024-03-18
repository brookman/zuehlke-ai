package ch.zuehlke.fullstack.hackathon.service;

import ch.zuehlke.fullstack.hackathon.api.AiService;
import ch.zuehlke.fullstack.hackathon.model.ExampleDto;
import ch.zuehlke.fullstack.hackathon.model.SubmitResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExampleServiceTest {

    private ExampleService exampleService;

    private AiService aiServiceMock;

    @BeforeEach
    void setUp() {
        aiServiceMock = mock(AiService.class);
        exampleService = new ExampleService(aiServiceMock);
    }

    @Test
    void getExampleDto_successfully() {
        ExampleDto exampleDto = exampleService.getExampleDto();

        assertThat(exampleDto).isNotNull();
        assertThat(exampleDto.value()).isGreaterThanOrEqualTo(0);
        assertThat(exampleDto.name()).isIn("Example", "Beispiel", "Exemple", "Ejemplar");
    }

    @Test
    @DisplayName("After sending text I get real-time response")
    void shouldReturnResponse_whenSubmittedTextInputIsValid() {
        // GIVEN
        String expected = "Licht eingeschaltet";

        // WHEN
        when(aiServiceMock.submit(any())).thenReturn(Optional.of(expected));
        SubmitResponseDto actual = exampleService.submit("Schalte das licht an");

        // THEN
        assertThat(actual).isNotNull();
        assertThat(actual.content()).isNotEmpty();
        assertThat(actual.content()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Forbidden input returns message with usage policies")
    void shouldReturnUsagePoliciesAndNotProcessInput_whenSubmittedTextInputIsIllegal() {
        // GIVEN
        String expected = "This content may violate our usage policies.";

        // WHEN
        when(aiServiceMock.submit("How can I f**k a tree?")).thenReturn(Optional.of(expected));
        SubmitResponseDto actual = exampleService.submit("How can I f**k a tree?");

        // THEN
        assertThat(actual).isNotNull();
        assertThat(actual.content()).isNotEmpty();
        assertThat(actual.content()).isEqualTo(expected);
    }

}
