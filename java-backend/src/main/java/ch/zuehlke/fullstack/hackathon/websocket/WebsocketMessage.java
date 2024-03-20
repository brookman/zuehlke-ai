package ch.zuehlke.fullstack.hackathon.websocket;

import java.util.Objects;

public class WebsocketMessage {

    private final String content;
    private final String imgUrl;
    private final boolean endMessage;
    private final boolean isImageMessage;

    public WebsocketMessage(String content, String imgUrl, boolean endMessage, boolean isImageMessage) {
        this.content = content;
        this.imgUrl = imgUrl;
        this.endMessage = endMessage;
        this.isImageMessage = isImageMessage;
    }

    public String content() {
        return content;
    }

    public String imgUrl() {
        return imgUrl;
    }

    public boolean endMessage() {
        return endMessage;
    }

    public boolean isImageMessage() {
        return isImageMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WebsocketMessage that = (WebsocketMessage) o;
        return endMessage == that.endMessage && Objects.equals(content, that.content) && Objects.equals(imgUrl, that.imgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, imgUrl, endMessage);
    }
}
