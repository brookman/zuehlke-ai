package ch.zuehlke.fullstack.hackathon.api;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.BistroAction;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.LightAction;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model.LightSwitch;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActionFactory {
    public final List<Action> allowedActions;

    public ActionFactory(LightSwitch lightSwitch) {
        allowedActions = List.of(new LightAction(lightSwitch), new BistroAction());
    }

    Optional<Action> getAction(String name) {
        return allowedActions.stream()
                .filter(action -> action.canHandle(name))
                .findFirst();
    }
}
