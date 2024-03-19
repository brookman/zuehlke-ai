package ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model;

public class LightSwitch {
    private static LightSwitch instance;

    private boolean isOn;

    private LightSwitch() {
        isOn = false;
    }

    public static synchronized LightSwitch getInstance() {
        if (instance == null) {
            instance = new LightSwitch();
        }
        return instance;
    }

    public void setStatus(boolean value) {
        isOn = value;
    }
}


