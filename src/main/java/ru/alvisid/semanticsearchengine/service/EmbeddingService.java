package ru.alvisid.semanticsearchengine.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.dto.Tokens;

import java.util.HashMap;
import java.util.Map;

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

    // Метод для генерации эмбеддинга из текста
    public float[] generateEmbedding(String text) {
        try {
            log.info("Генерация эмбеддинга для текста: {}", text);

            // 1. Токенизация
            Tokens tokens = tokenizerService.tokenize(text);
            long[] inputIds = tokens.inputIds();
            long[] attentionMask = tokens.attentionMask();

            // 2. Создаем тензоры для ONNX Runtime
            // Форма: [batch_size, sequence_length] = [1, length]
            long[][] inputIdsBatch = {inputIds};
            long[][] attentionMaskBatch = {attentionMask};

            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(ortEnvironment, inputIdsBatch);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(ortEnvironment, attentionMaskBatch);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);

            // 3. Запускаем инференс
            try (OrtSession.Result results = ortSession.run(inputs)) {
                // Результат — тензор с эмбеддингами
                OnnxTensor outputTensor = (OnnxTensor) results.get("embeddings").get();
                float[][][] outputArray = (float[][][]) outputTensor.getValue();

                // Извлекаем эмбеддинг первого (и единственного) текста в батче
                float[] embedding = outputArray[0][0]; // [batch][sequence][features]

                log.info("Эмбеддинг сгенерирован. Размерность: {}", embedding.length);
                return embedding;
            }

        } catch (Exception e) {
            log.error("Ошибка при генерации эмбеддинга для текста: {}", text, e);
            throw new RuntimeException("Ошибка генерации эмбеддинга", e);
        }
    }
}
