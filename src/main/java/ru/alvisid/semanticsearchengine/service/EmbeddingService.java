package ru.alvisid.semanticsearchengine.service;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    // Метод для генерации эмбеддинга из текста
    public float[] generateEmbedding(String text) {
        try {
            log.debug("Генерация эмбеддинга для текста: {}", text);

            // TODO: Здесь нужна токенизация!
            // Пока мы не реализовали токенизацию в Java, мы не можем подать текст напрямую.
            // Временно заглушка: возвращаем заглушку, чтобы проверить, что модель загружается.
            // На следующем шаге мы добавим токенизацию.

            log.warn("Токенизация ещё не реализована! Возвращаем заглушку.");
            return new float[384]; // Заглушка

            /*
            // В реальности, когда добавим токенизацию, здесь будет:
            // 1. Токенизация текста → input_ids, attention_mask
            // 2. Создание OnnxTensor из токенов
            // 3. Запуск инференса
            // 4. Получение вектора
            */

        } catch (Exception e) {
            log.error("Ошибка при генерации эмбеддинга для текста: {}", text, e);
            throw new RuntimeException("Ошибка генерации эмбеддинга", e);
        }
    }
}
