package ru.alvisid.semanticsearchengine.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * @author EGlushkov
 * Date: 02.07.2026
 * Time: 22:06
 */

@Slf4j
@Configuration
public class OnnxModelConfig {

    @Value("${ml.model.embedding.path}")
    private Resource modelResource;

    private OrtEnvironment environment;

    @Bean
    public OrtEnvironment ortEnvironment() {
        log.info("Создание ONNX Runtime environment");
        return OrtEnvironment.getEnvironment();
    }

    @Bean
    @ConditionalOnProperty(name = "ml.model.enabled", havingValue = "true", matchIfMissing = true)
    public OrtSession ortSession(OrtEnvironment env) throws Exception {
        log.info("Загрузка модели...");

        // Получаем путь к модели как строку
        String modelPath = modelResource.getFile().getAbsolutePath();
        log.info("Путь к модели: {}", modelPath);

        // Создаем сессию через путь к файлу
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        OrtSession session = env.createSession(modelPath, options);

        log.info("✅ Модель успешно загружена");
        return session;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Закрытие ONNX Runtime environment");
        if (environment != null) {
            try {
                environment.close();
                log.info("ONNX Runtime environment успешно закрыт");
            } catch (Exception e) {
                log.error("Ошибка при закрытии ONNX Runtime environment", e);
            }
        }
    }
}
