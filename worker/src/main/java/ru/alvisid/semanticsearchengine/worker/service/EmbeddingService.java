package ru.alvisid.semanticsearchengine.worker.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.worker.dto.Tokens;
import ru.alvisid.semanticsearchengine.worker.model.EmbeddingEntity;
import ru.alvisid.semanticsearchengine.worker.repository.EmbeddingRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:32
 */

@Slf4j
@Service
public class EmbeddingService {

    private final OrtSession ortSession;
    private final OrtEnvironment ortEnvironment;
    private final TokenizerService tokenizerService;
    private final EmbeddingRepository embeddingRepository;

    public EmbeddingService(@Qualifier("embeddingSession") OrtSession ortSession,
                            OrtEnvironment ortEnvironment,
                            TokenizerService tokenizerService,
                            EmbeddingRepository embeddingRepository) {
        this.ortSession = ortSession;
        this.ortEnvironment = ortEnvironment;
        this.tokenizerService = tokenizerService;
        this.embeddingRepository = embeddingRepository;
    }

    public Optional<EmbeddingEntity> getByText(String text) {
        return embeddingRepository.findByText(text);
    }

    // Метод для генерации и сохранения эмбеддинга
    public void generateAndSaveEmbedding(String text) {
        log.info("Генерация и сохранение эмбеддинга для текста: {}", text);

        if (existsByText(text)) {
            log.info("Эмбеддинг с текстом {} существует. Процесс прерван.", text);
            return;
        }

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

    public boolean existsByText(String text) {
        return embeddingRepository.findOneCountByText(text) > 0;
    }
}