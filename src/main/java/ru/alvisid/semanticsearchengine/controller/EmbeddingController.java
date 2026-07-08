package ru.alvisid.semanticsearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.dto.EmbeddingRequest;
import ru.alvisid.semanticsearchengine.dto.EmbeddingResponse;
import ru.alvisid.semanticsearchengine.service.EmbeddingService;

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

    @PostMapping
    public EmbeddingResponse generateEmbedding(@RequestBody EmbeddingRequest request) {
        log.info("Получен запрос на генерацию эмбеддинга для текста: {}", request.getText());

        float[] embedding = embeddingService.generateAndSaveEmbedding(request.getText());
        return new EmbeddingResponse(embedding);
    }
}
