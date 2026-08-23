package com.musicapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables real-time push updates (playlist/favourite changes) to all of a
 * user's connected devices — used alongside the REST /api/sync/pull and
 * /api/sync/push endpoints for full offline-first mobile sync.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Clients subscribe to /topic/user/{userId}/sync for live updates
        registry.enableSimpleBroker("/topic");
        // Clients send messages prefixed with /app (not required for pure push-only sync)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tighten in production to app.cors.allowed-origins
                .withSockJS(); // fallback for clients without native WebSocket support
    }
}
