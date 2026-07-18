package ru.alvisid.semanticsearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.dto.EmbeddingRequest;
import ru.alvisid.semanticsearchengine.dto.EmbeddingResponse;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.service.EmbeddingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:34
 */

@Slf4j
@RestController
@RequestMapping("/api/embed")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @PostMapping("/get-by-text")
    public ResponseEntity<EmbeddingEntity> getEmbeddingByText(@RequestBody SearchRequest request) {
        // Ищем эмбеддинг в БД по тексту
        Optional<EmbeddingEntity> entity = embeddingService.getByText(request.getQuery());
        return entity.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public EmbeddingResponse generateEmbedding(@RequestBody EmbeddingRequest request) {
        log.info("Получен запрос на генерацию эмбеддинга для текста: {}", request.getText());

        float[] embedding = embeddingService.generateAndSaveEmbedding(request.getText());
        return new EmbeddingResponse(embedding);
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> generateEmbeddingsBatch(@RequestBody List<EmbeddingRequest> requests) {
        log.info("Получен запрос на пакетную генерацию эмбеддингов для {} текстов", requests.size());

        for (EmbeddingRequest req : requests) {
            embeddingService.generateAndSaveEmbedding(req.getText());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("processed", requests.size());

        return ResponseEntity.accepted().body(response);
    }
}
