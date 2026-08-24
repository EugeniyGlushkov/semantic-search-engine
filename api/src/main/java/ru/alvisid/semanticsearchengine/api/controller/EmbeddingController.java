package ru.alvisid.semanticsearchengine.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alvisid.semanticsearch.grpc.EmbedRequest;
import ru.alvisid.semanticsearch.grpc.EmbedResponse;
import ru.alvisid.semanticsearch.grpc.SearchRequest;
import ru.alvisid.semanticsearch.grpc.SearchResponse;
import ru.alvisid.semanticsearch.grpc.SemanticSearchServiceGrpc;
import ru.alvisid.semanticsearchengine.api.dto.EmbeddingRequestDto;
import ru.alvisid.semanticsearchengine.api.dto.EmbeddingResponseDto;
import ru.alvisid.semanticsearchengine.api.dto.SearchRequestDto;
import ru.alvisid.semanticsearchengine.api.dto.SearchResponseDto;
import ru.alvisid.semanticsearchengine.api.producer.EmbeddingProducer;

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

    private final EmbeddingProducer producer;
    private final SemanticSearchServiceGrpc.SemanticSearchServiceBlockingStub grpcStub;

    @PostMapping
    public ResponseEntity<Void> embed(@RequestBody EmbeddingRequestDto request) {
        producer.send(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> generateEmbeddingsBatch(@RequestBody List<EmbeddingRequestDto> requests) {
        log.info("Получен запрос на пакетную генерацию эмбеддингов для {} текстов", requests.size());

        requests.forEach(producer::send);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("processed", requests.size());

        return ResponseEntity.accepted().body(response);
    }

    @PostMapping("/get-by-text")
    public ResponseEntity<EmbeddingResponseDto> getEmbeddingByText(@RequestBody EmbeddingRequestDto request) {
        EmbedRequest grpcRequest = EmbedRequest.newBuilder()
                .setText(request.getText())
                .build();
        EmbedResponse grpcResponse = grpcStub.getEmbedByText(grpcRequest);
        List<Float> embeddings = grpcResponse.getEmbeddingList();

        if (embeddings.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            EmbeddingResponseDto response = EmbeddingResponseDto.builder()
                    .text(request.getText())
                    .embedding(embeddings)
                    .build();
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/search")
    public SearchResponseDto search(@RequestBody SearchRequestDto request) {
        String query = request.getQuery();
        log.info("Поиск по запросу: {}", query);
        SearchRequest grpcRequest = SearchRequest.newBuilder()
                .setQuery(request.getQuery())
                .setTopK(request.getTopK())
                .setRerank(request.isRerank())
                .build();
        SearchResponse grpcResponse = grpcStub.search(grpcRequest);
        return new SearchResponseDto(grpcResponse.getTextsList());
    }
}