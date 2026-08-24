package ru.alvisid.semanticsearchengine.api.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.api.dto.EmbeddingRequestDto;

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
        kafkaTemplate.send(MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, "embedding-requests")
                .setHeader("__TypeId__", "embeddingRequestDto")
                .build());
    }
}