package ch.zuehlke.fullstack.hackathon.api;

import ch.zuehlke.fullstack.hackathon.dynamicfunction.Action;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.bistro.BistroAction;
import ch.zuehlke.fullstack.hackathon.dynamicfunction.light.LightAction;

import java.util.List;
import java.util.Optional;

public class ActionFactory {
    private static final List<Action> registeredActions = List.of(new LightAction(), new BistroAction());

    static Optional<Action> getAction(String name) {
        return registeredActions.stream()
                .filter(action -> action.canHandle(name))
                .findFirst();
    }
}
