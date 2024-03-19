package ch.zuehlke.fullstack.hackathon.service;

import ch.zuehlke.fullstack.hackathon.api.AiService;
import ch.zuehlke.fullstack.hackathon.model.ExampleDto;
import ch.zuehlke.fullstack.hackathon.model.MessageOfTheDayDto;
import ch.zuehlke.fullstack.hackathon.model.SubmitResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private static final List<String> POSSIBLE_VALUES = List.of("Example", "Beispiel", "Exemple", "Ejemplar");
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private final AiService aiService;

    public ExampleDto getExampleDto() {
        int randomInt = RANDOM.nextInt(POSSIBLE_VALUES.size());

        return new ExampleDto(POSSIBLE_VALUES.get(randomInt), randomInt);
    }

    public MessageOfTheDayDto getMessageOfTheDayDto() {
        Optional<String> content = aiService.getMessageOfTheDay();
        Optional<String> catImageUrl = aiService.getCatImageUrl();
        return new MessageOfTheDayDto(content.orElse("Could not get any message."), catImageUrl.orElse(null));
    }

    public SubmitResponseDto submit(String input) {
        Optional<String> content = aiService.submit(input);
        return new SubmitResponseDto(content.orElse("Could not get any message."));
    }
}
