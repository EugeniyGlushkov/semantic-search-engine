package ru.alvisid.semanticsearchengine.worker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.worker.dto.ScoredEntity;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.repository.EmbeddingRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author EGlushkov
 * Date: 13.08.2026
 * Time: 18:08
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    public static int DEFAULT_TOP_CANDIDATES = 50;

    private final EmbeddingService embeddingService;
    private final CrossEncoderService crossEncoderService;
    private final EmbeddingRepository embeddingRepository;

    public List<EmbeddingEntity> search(String query, int topK, boolean rerank) {
        // 1. Быстрый поиск через pgvector (берём больше кандидатов, например, 50)
        int candidatesCount = rerank || topK == 0 ? DEFAULT_TOP_CANDIDATES : topK;
        float[] queryEmbedding = embeddingService.getEmbedding(query);
        String vectorString = Arrays.toString(queryEmbedding);
        List<EmbeddingEntity> candidates =
                embeddingRepository.findNearestByEmbedding(vectorString, candidatesCount);

        if (!rerank) {
            return candidates;
        }

        // 2. Реранжинг через Cross-Encoder
        return candidates.stream()
                .map(doc -> {
                    float score = crossEncoderService.score(query, doc.getText());
                    return new ScoredEntity(doc, score);
                })
                .sorted((a, b) -> Float.compare(b.score(), a.score()))
                .limit(topK)
                .map(ScoredEntity::entity)
                .collect(Collectors.toList());
    }
}
