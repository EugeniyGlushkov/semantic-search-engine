package ru.alvisid.semanticsearchengine.worker.drift;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.worker.drift.monitors.FeatureDriftMonitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author EGlushkov
 * Date: 28.09.2026
 * Time: 11:26
 */

@Slf4j
@Component
public class DriftStatisticsCollector {

    private final List<FeatureDriftMonitor> monitors;

    public DriftStatisticsCollector(List<FeatureDriftMonitor> monitors) {
        if (monitors == null || monitors.isEmpty()) {
            throw new IllegalStateException("At least one FeatureDriftMonitor must be registered");
        }
        this.monitors = Collections.unmodifiableList(monitors);
        log.info("DriftStatisticsCollector инициализирован с {} мониторами: {}",
                monitors.size(),
                monitors.stream().map(FeatureDriftMonitor::getName).collect(Collectors.joining(", ")));
    }

    public void record(String query) {
        for (FeatureDriftMonitor monitor : monitors) {
            try {
                monitor.record(query);
            } catch (Exception e) {
                log.error("Ошибка в мониторе '{}' при обработке запроса", monitor.getName(), e);
            }
        }
    }

    public Map<String, DriftSnapshot> computeAllPSI() {
        return monitors.stream()
                .collect(Collectors.toMap(
                        FeatureDriftMonitor::getName,
                        FeatureDriftMonitor::computePSI));
    }

    public List<FeatureDriftMonitor> getMonitors() {
        return monitors;
    }

    public void reset() {
        monitors.forEach(FeatureDriftMonitor::reset);
    }
}
