package ch.zuehlke.fullstack.hackathon.dynamicfunction.light.model;

import org.springframework.stereotype.Service;

@Service
public class LightSwitch {

    public enum Color {
        OFF,
        RED,
        GREEN,
        BLUE,
        YELLOW,
        CYAN,
        MAGENTA,
        WHITE,
        DISCO
    }

    private Color color;

    private LightSwitch() {
        color = Color.OFF;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}


