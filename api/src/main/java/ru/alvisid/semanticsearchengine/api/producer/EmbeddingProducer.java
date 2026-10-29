package ru.alvisid.semanticsearchengine.api.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.dto.EmbeddingRequestDto;

/**
 * @author EGlushkov
 * Date: 26.07.2026
 * Time: 0:44
 */

@Component
public class EmbeddingProducer {
    private final KafkaTemplate<String, EmbeddingRequestDto> kafkaTemplate;

    public EmbeddingProducer(KafkaTemplate<String, EmbeddingRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(EmbeddingRequestDto request) {
        kafkaTemplate.send("embedding-requests", request);
    }
}