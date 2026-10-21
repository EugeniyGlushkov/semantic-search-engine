package ru.alvisid.semanticsearchengine.worker.drift;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.worker.drift.monitors.FeatureDriftMonitor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static ru.alvisid.semanticsearchengine.worker.drift.Histogram.MIN_OBSERVATIONS_FOR_PSI;

/**
 * @author EGlushkov
 * Date: 08.10.2026
 * Time: 10:52
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DriftMetricsPublisher {

    private static final double PSI_WARNING_THRESHOLD = 0.2;

    private final DriftStatisticsCollector collector;
    private final MeterRegistry meterRegistry;

    private final Map<String, AtomicReference<Double>> psiStorage = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> observationsStorage = new ConcurrentHashMap<>();

    @Scheduled(cron = "0 0 * * * *")
    public void publishMetrics() {
        log.info("Запуск периодического расчёта PSI...");

        for (FeatureDriftMonitor monitor : collector.getMonitors()) {
            String feature = monitor.getName();
            DriftSnapshot snapshot = monitor.computePSI();
            long totalCount = snapshot.count();
            double psi = snapshot.psi();

            psiStorage.computeIfAbsent(feature, f -> {
                AtomicReference<Double> holder = new AtomicReference<>(psi);
                meterRegistry.gauge("drift.psi", Tags.of("feature", f), holder, AtomicReference::get);
                return holder;
            }).set(psi);

            observationsStorage.computeIfAbsent(feature, f -> {
                AtomicLong holder = new AtomicLong(totalCount);
                meterRegistry.gauge("drift.observations", Tags.of("feature", f), holder);
                return holder;
            }).set(totalCount);

            if (Double.isNaN(psi)) {
                log.info("Для фичи '{}' недостаточно наблюдений ({}/{}). " +
                        "Данные НЕ сбрасываются, копим дальше.", feature, totalCount, MIN_OBSERVATIONS_FOR_PSI);
                continue;
            }

            monitor.reset();

            if (psi >= PSI_WARNING_THRESHOLD) {
                log.warn("Обнаружен дрейф: feature='{}', PSI={}, наблюдений={}", feature, psi, totalCount);
            } else {
                log.info("Drift check: feature='{}', PSI={}, наблюдений={}", feature, psi, totalCount);
            }
        }
    }
}