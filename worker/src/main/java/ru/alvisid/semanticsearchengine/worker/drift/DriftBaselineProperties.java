package ru.alvisid.semanticsearchengine.worker.drift;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author EGlushkov
 * Date: 27.09.2026
 * Time: 21:46
 */

/*
 * Конфигурация бейзлайнов для мониторинга дрейфа.
 * Загружается из секции {@code drift.baseline} в application.yml.
 */

@Data
@Component
@ConfigurationProperties(prefix = "drift.baseline")
public class DriftBaselineProperties {
    /** Бейзлайн для длины запроса. */
    private FeatureBaseline length;
    /** Бейзлайн для OOV-доли */
    private FeatureBaseline oov;
}
