package ru.alvisid.semanticsearchengine.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.dto.SearchResponse;
import ru.alvisid.semanticsearchengine.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.service.SearchService;

import java.util.List;

/**
 * @author EGlushkov
 * Date: 08.07.2026
 * Time: 14:07
 */

@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public SearchResponse search(@RequestBody SearchRequest request) {
        String query = request.getQuery();
        log.info("Поиск по запросу: {}", query);
        List<String> texts = searchService.search(query).stream()
                .map(EmbeddingEntity::getText)
                .toList();
        return new SearchResponse(texts);
    }
}
