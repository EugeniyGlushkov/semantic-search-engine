package ru.alvisid.semanticsearchengine.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

/**
 * @author EGlushkov
 * Date: 09.07.2026
 * Time: 20:27
 */

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public KafkaTemplate mockKafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}