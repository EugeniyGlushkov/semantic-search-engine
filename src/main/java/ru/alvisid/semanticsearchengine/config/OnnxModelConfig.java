package ru.alvisid.semanticsearchengine.config;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;

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
    public OrtSession ortSession() throws Exception {
        log.info("Загрузка модели из: {}", modelResource.getURI());

        // Получаем файл из ресурсов
        File modelFile = modelResource.getFile();

        // Создаем сессию, передавая путь к файлу
        OrtSession session = ortEnvironment().createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());

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
