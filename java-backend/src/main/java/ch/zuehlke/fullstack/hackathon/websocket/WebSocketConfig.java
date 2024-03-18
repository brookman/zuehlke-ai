package ch.zuehlke.fullstack.hackathon.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(), "/ws")
                .setAllowedOrigins("*")
                // initial Request/Handshake interceptor
                .addInterceptors(new HttpSessionHandshakeInterceptor() {

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, org.springframework.web.socket.WebSocketHandler wsHandler, @Nullable Exception ex) {
                        super.afterHandshake(request, response, wsHandler, ex);
                    }

                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request,
                                                   ServerHttpResponse response, org.springframework.web.socket.WebSocketHandler wsHandler,
                                                   Map<String, Object> attributes) throws Exception {
                        boolean b = super.beforeHandshake(request, response, wsHandler, attributes);
                        // && (request.getPrincipal()).isAuthenticated();
                        return b;
                    }

                });

    }
}