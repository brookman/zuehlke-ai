package ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model;

public class LightSwitch {
    private static LightSwitch instance;

    private boolean on;

    private LightSwitch() {
        on = false;
    }

    public static synchronized LightSwitch getInstance() {
        if (instance == null) {
            instance = new LightSwitch();
        }
        return instance;
    }

    public void setStatus(boolean value) {
        on = value;
    }

    public boolean isOn() {
        return on;
    }
}


