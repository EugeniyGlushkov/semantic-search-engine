package ru.alvisid.semanticsearchengine.worker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.dto.SearchResponse;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.service.SearchService;

import java.util.List;

/**
 * @author EGlushkov
 * Date: 13.08.2026
 * Time: 22:26
 */

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @PostMapping()
    public SearchResponse search(@RequestBody SearchRequest request) {
        String query = request.getQuery();
        log.info("Поиск по запросу: {}", query);
        List<String> texts = searchService.search(query, request.getTopK(), request.isRerank()).stream()
                .map(EmbeddingEntity::getText)
                .toList();
        return new SearchResponse(texts);
    }
}
