package ru.alvisid.semanticsearchengine.worker.service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.alvisid.semanticsearchengine.worker.dto.Tokens;

import java.util.HashMap;
import java.util.Map;

/**
 * @author EGlushkov
 * Date: 12.08.2026
 * Time: 23:05
 */

@Slf4j
@Service
public class CrossEncoderService {

    private final OrtSession session;
    private final OrtEnvironment environment;
    private final TokenizerService tokenizerService;

    public CrossEncoderService(@Qualifier("crossEncoderSession") OrtSession session,
                               OrtEnvironment environment,
                               TokenizerService tokenizerService) {
        this.session = session;
        this.environment = environment;
        this.tokenizerService = tokenizerService;
    }

    /**
     * Оценивает релевантность пары (запрос, документ).
     * @param query запрос пользователя
     * @param document текст документа
     * @return оценка релевантности (число, чем выше — тем релевантнее)
     */
    public float score(String query, String document) {
        try {
            // 1. Токенизация пары
            Tokens tokens = tokenizerService.tokenizePair(query, document);

            // Создаем тензоры из токенов
            long[][] inputIdsBatch = {tokens.inputIds()};
            long[][] attentionMaskBatch = {tokens.attentionMask()};

            OnnxTensor inputIdsTensor = OnnxTensor.createTensor(environment, inputIdsBatch);
            OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(environment, attentionMaskBatch);

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", inputIdsTensor);
            inputs.put("attention_mask", attentionMaskTensor);
            float result;

            try (OrtSession.Result results = session.run(inputs)) {
                OnnxTensor outputTensor = (OnnxTensor) results.get("logits").get();
                float[][] output = (float[][]) outputTensor.getValue();

                // Проверяем размерность выходного массива
                if (output[0].length == 1) {
                    // Если один логит — это и есть релевантность
                    result = output[0][0];
                } else if (output[0].length == 2) {
                    // Если два логита — берём позитивный (индекс 1)
                    result = output[0][1];
                } else {
                    log.warn("Неожиданная размерность выходного массива: {}", output[0].length);
                    return 0.0f;
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Ошибка при оценке релевантности пары: {} | {}", query, document, e);
            return 0.0f;
        }
    }
}
