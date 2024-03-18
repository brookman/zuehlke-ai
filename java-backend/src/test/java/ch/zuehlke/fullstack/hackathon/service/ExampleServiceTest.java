package ch.zuehlke.fullstack.hackathon.service;

import ch.zuehlke.fullstack.hackathon.api.AiService;
import ch.zuehlke.fullstack.hackathon.model.ExampleDto;
import ch.zuehlke.fullstack.hackathon.model.SubmitResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
    void submit_successfully() {
        SubmitResponseDto actual = exampleService.submit("Schalte das licht an");

        assertThat(actual).isNotNull();
        assertThat(actual.content()).isNotEmpty();
    }


}
