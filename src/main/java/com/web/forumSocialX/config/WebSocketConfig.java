package com.web.forumSocialX.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Active le broker simple pour les sujets et files d'attente
        registry.enableSimpleBroker("/topic", "/user", "/queue"); // Active un broker simple pour les sujets et files d'attente
        registry.setApplicationDestinationPrefixes("/app"); // Préfixe pour les messages de l'application
        registry.setUserDestinationPrefix("/user"); // Le préfixe pour les messages spécifiques à un utilisateur
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Ajouter le Socket pour /chat-socket avec la configuration de la taille maximale de trame
        registry.addEndpoint("/chat-socket")
                .setAllowedOrigins(
                        "https://forum-socialx.vercel.app", "http://localhost:4200", "http://localhost:4300",
                        "capacitor://localhost", "http://localhost", "http://192.168.1.14:8084",
                        "http://192.168.1.17:8084", "http://10.0.2.2:8084", "http://192.168.x.x:8084",
                        "http://forum-social-x-frontend-takoua1-dev.apps.rm3.7wse.p1.openshiftapps.com")

                .withSockJS();

        // Ajouter un autre Socket pour /ws-signale
        registry.addEndpoint("/ws-signale")
                .setAllowedOrigins(
                        "https://forum-socialx.vercel.app", "http://localhost:4200", "http://localhost:4300",
                        "capacitor://localhost", "http://localhost", "http://192.168.1.14:8084",
                        "http://192.168.1.17:8084", "http://10.0.2.2:8084", "http://192.168.x.x:8084",
                        "http://forum-social-x-frontend-takoua1-dev.apps.rm3.7wse.p1.openshiftapps.com")

                .withSockJS();

        // Ajouter un autre Socket pour /ws-mail
        registry.addEndpoint("/ws-mail")
                .setAllowedOrigins(
                        "https://forum-socialx.vercel.app", "http://localhost:4200", "http://localhost:4300",
                        "capacitor://localhost", "http://localhost", "http://192.168.1.14:8084",
                        "http://192.168.1.17", "http://10.0.2.2:8084", "http://192.168.x.x:8084",
                        "http://forum-social-x-frontend-takoua1-dev.apps.rm3.7wse.p1.openshiftapps.com")

                .withSockJS() ;
    }




    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Configuration des convertisseurs de messages
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.registerModule(new JavaTimeModule());

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);

        messageConverters.add(converter);
        return false; // Laisser Spring ajouter ses convertisseurs par défaut
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Augmenter la limite de taille des messages et des tampons d'envoi
        registration.setMessageSizeLimit(104857600); // 100 Mo
        registration.setSendBufferSizeLimit(104857600); // 100 Mo
        registration.setSendTimeLimit(20000); // Temps d'attente pour l'envoi des messages (20 secondes)
    }

  /*  @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean factoryBean = new ServletServerContainerFactoryBean();
        factoryBean.setMaxTextMessageBufferSize(104857600); // 100 Mo pour les messages texte
        factoryBean.setMaxBinaryMessageBufferSize(104857600); // 100 Mo pour les messages binaires
        factoryBean.setMaxSessionIdleTimeout(30000L); // Timeout de session après 30 secondes
        factoryBean.setAsyncSendTimeout(20000L); // Timeout de l'envoi asynchrone (20 secondes)
        return factoryBean;
    }
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }*/


    @Bean
    public ThreadPoolTaskExecutor clientInboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor clientOutboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }



}
