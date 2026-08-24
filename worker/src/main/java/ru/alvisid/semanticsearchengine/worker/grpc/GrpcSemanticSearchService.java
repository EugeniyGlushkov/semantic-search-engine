package ru.alvisid.semanticsearchengine.worker.grpc;

import com.google.common.primitives.Floats;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.alvisid.semanticsearch.grpc.EmbedRequest;
import ru.alvisid.semanticsearch.grpc.EmbedResponse;
import ru.alvisid.semanticsearch.grpc.SearchRequest;
import ru.alvisid.semanticsearch.grpc.SearchResponse;
import ru.alvisid.semanticsearch.grpc.SemanticSearchServiceGrpc;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.service.EmbeddingService;
import ru.alvisid.semanticsearchengine.worker.service.SearchService;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author EGlushkov
 * Date: 23.08.2026
 * Time: 17:42
 */

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcSemanticSearchService extends SemanticSearchServiceGrpc.SemanticSearchServiceImplBase {
    private final SearchService searchService;
    private final EmbeddingService embeddingService;

    @Override
    public void search(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        log.info("gRPC поиск: query='{}', topK={}, rerank={}",
                request.getQuery(), request.getTopK(), request.getRerank());

        try {
            var results = searchService.search(
                    request.getQuery(),
                    request.getTopK(),
                    request.getRerank()
            );

            SearchResponse response = SearchResponse.newBuilder()
                    .addAllTexts(results.stream()
                            .map(doc -> doc.getText())
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при поиске: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getEmbedByText(EmbedRequest request, StreamObserver<EmbedResponse> responseObserver) {
        log.info("gRPC получение эмбеддинга для текста: '{}'", request.getText());

        try {
            Optional<EmbeddingEntity> embeddingOpt = embeddingService.getByText(request.getText());

            if (embeddingOpt.isPresent()) {
                float[] embedding = embeddingOpt.get().getEmbedding();
                EmbedResponse response = EmbedResponse.newBuilder()
                        .addAllEmbedding(Floats.asList(embedding))
                        .build();
                responseObserver.onNext(response);
            } else {
                EmbedResponse response = EmbedResponse.newBuilder()
                        .addAllEmbedding(Collections.emptyList())
                        .build();
                responseObserver.onNext(response);
            }

            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при получении эмбеддинга: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }
}