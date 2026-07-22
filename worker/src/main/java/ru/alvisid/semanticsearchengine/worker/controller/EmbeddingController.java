package ru.alvisid.semanticsearchengine.worker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.dto.SearchResponse;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.service.EmbeddingService;

import java.util.List;
import java.util.Optional;

/**
 * @author EGlushkov
 * Date: 08.07.2026
 * Time: 14:07
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

    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) {
        String query = request.getQuery();
        log.info("Поиск по запросу: {}", query);
        List<String> texts = embeddingService.search(query).stream()
                .map(EmbeddingEntity::getText)
                .toList();
        return new SearchResponse(texts);
    }
}