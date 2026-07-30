package ru.alvisid.semanticsearchengine.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author EGlushkov
 * Date: 30.07.2026
 * Time: 15:45
 */

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic embeddingRequestsTopic() {
        // Имя топика, количество партиций (3), фактор репликации (1)
        return new NewTopic("embedding-requests", 3, (short) 1);
    }
}