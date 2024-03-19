package ch.zuehlke.fullstack.hackathon.websocket;

import java.util.Objects;

public class WebsocketMessage {

    private final String content;
    private final boolean endMessage;

    public WebsocketMessage(String content, boolean endMessage) {
        this.content = content;
        this.endMessage = endMessage;
    }

    public String content() {
        return content;
    }

    public boolean endMessage() {
        return endMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebsocketMessage that = (WebsocketMessage) o;
        return endMessage == that.endMessage && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, endMessage);
    }
}
