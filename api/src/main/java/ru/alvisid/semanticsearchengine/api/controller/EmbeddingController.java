package ru.alvisid.semanticsearchengine.api.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearchengine.api.client.WorkerClient;
import ru.alvisid.semanticsearchengine.api.dto.EmbeddingResponse;
import ru.alvisid.semanticsearchengine.api.producer.EmbeddingProducer;
import ru.alvisid.semanticsearchengine.dto.EmbeddingRequest;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.dto.SearchResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final WorkerClient client;
    private final EmbeddingProducer producer;

    @PostMapping
    public ResponseEntity<Void> embed(@RequestBody EmbeddingRequest request) {
        producer.send(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> generateEmbeddingsBatch(@RequestBody List<EmbeddingRequest> requests) {
        log.info("Получен запрос на пакетную генерацию эмбеддингов для {} текстов", requests.size());

        requests.forEach(producer::send);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("processed", requests.size());

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/get-by-text")
    public ResponseEntity<EmbeddingResponse> getEmbeddingByText(@RequestBody SearchRequest request) {
        try {
            EmbeddingResponse response = client.getByText(request);
            return ResponseEntity.ok(response);
        } catch (FeignException.NotFound e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/search")
    public SearchResponse search(@RequestBody SearchRequest request) {
        String query = request.getQuery();
        log.info("Поиск по запросу: {}", query);
        return client.search(request);
    }
}