package ru.alvisid.semanticsearchengine.worker.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.worker.dto.EmbeddingRequestDto;
import ru.alvisid.semanticsearchengine.worker.service.EmbeddingService;

/**
 * @author EGlushkov
 * Date: 26.07.2026
 * Time: 21:16
 */

@Component
@Slf4j
public class EmbeddingConsumer {
    private final EmbeddingService embeddingService;

    public EmbeddingConsumer(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @KafkaListener(topics = "embedding-requests", groupId = "worker-group")
    public void consume(EmbeddingRequestDto request) {
        log.info("Получен запрос на генерацию эмбеддинга: {}", request.getText());
        embeddingService.generateAndSaveEmbedding(request.getText());
    }
}