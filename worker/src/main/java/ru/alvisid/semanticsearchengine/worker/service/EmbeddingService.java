package ru.alvisid.semanticsearchengine.worker.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.worker.dto.Tokens;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.repository.EmbeddingRepository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:32
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final OrtSession ortSession;
    private final OrtEnvironment ortEnvironment;
    private final TokenizerService tokenizerService;
    private final EmbeddingRepository embeddingRepository;

    public Optional<EmbeddingEntity> getByText(String text) {
        return embeddingRepository.findByText(text);
    }

    // Метод для генерации и сохранения эмбеддинга
    public void generateAndSaveEmbedding(String text) {
        log.info("Генерация и сохранение эмбеддинга для текста: {}", text);

        // 1. Генерируем эмбеддинг
        float[] embedding = getEmbedding(text);

        // 3. Сохраняем в БД
        EmbeddingEntity entity = EmbeddingEntity.builder()
                .text(text)
                .embedding(embedding)
                .build();
        embeddingRepository.save(entity);

        log.info("Эмбеддинг сохранен. ID записи: {}", entity.getId());
    }

    public List<EmbeddingEntity> search(String query) {
        // 1. Генерируем эмбеддинг для запроса (не сохраняем его!)
        float[] queryEmbedding = getEmbedding(query); // ← только генерация, без сохранения
        String vectorString = Arrays.toString(queryEmbedding);

        // 2. Ищем в БД (используем репозиторий)
        return embeddingRepository.findNearestByEmbedding(vectorString, 5);
    }

    public float[] getEmbedding(String text) {
        try {
            // 1. Генерируем токены
            Tokens tokens = tokenizerService.tokenize(text);
            // Создаем тензоры из токенов
            long[][] inputIdsBatch = {tokens.inputIds()};
            long[][] attentionMaskBatch = {tokens.attentionMask()};

            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(ortEnvironment, inputIdsBatch);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(ortEnvironment, attentionMaskBatch);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            // Запускаем инференс
            try (OrtSession.Result results = ortSession.run(inputs)) {
                OnnxTensor outputTensor = (OnnxTensor) results.get("embeddings").get();
                float[][][] outputArray = (float[][][]) outputTensor.getValue();
                return outputArray[0][0]; // Возвращаем эмбеддинг
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении инференса", e);
            throw new RuntimeException("Ошибка инференса", e);
        }
    }
}