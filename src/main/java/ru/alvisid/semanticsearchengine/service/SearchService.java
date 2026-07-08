package ru.alvisid.semanticsearchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.repository.EmbeddingRepository;

import java.util.Arrays;
import java.util.List;

/**
 * @author EGlushkov
 * Date: 08.07.2026
 * Time: 13:57
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final EmbeddingService embeddingService;
    private final EmbeddingRepository embeddingRepository;

    public List<EmbeddingEntity> search(String query) {
        // 1. Генерируем эмбеддинг для запроса (не сохраняем его!)
        float[] queryEmbedding = embeddingService.getEmbedding(query); // ← только генерация, без сохранения
        String vectorString = Arrays.toString(queryEmbedding);

        // 2. Ищем в БД (используем репозиторий)
        return embeddingRepository.findNearestByEmbedding(vectorString, 5);
    }
}
