package ru.alvisid.semanticsearchengine.worker.drift.monitors;

import ru.alvisid.semanticsearchengine.worker.drift.DriftSnapshot;

public interface FeatureDriftMonitor {

    String getName();

    /**
     * Извлекает значение признака из запроса и добавляет его в гистограмму.
     * @param query текст запроса
     * @return true, если наблюдение было записано (false — если значение недоступно)
     */
    boolean record(String query);

    /** Вычисляет PSI относительно бейзлайна (0.0, если данных мало). */
    DriftSnapshot computePSI();

    /** Общее количество накопленных наблюдений. */
    long getTotalCount();

    /** Сбрасывает гистограмму (для начала нового периода). */
    void reset();
}