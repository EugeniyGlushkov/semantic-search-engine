package ru.alvisid.semanticsearchengine.worker.drift.monitors;

/**
 * @author EGlushkov
 * Date: 28.09.2026
 * Time: 11:20
 */

import org.springframework.stereotype.Component;
import ru.alvisid.semanticsearchengine.worker.drift.DriftBaselineProperties;

/**
 * Монитор дрейфа для длины запроса.
 */
@Component
public class LengthDriftMonitor extends AbstractFeatureDriftMonitor {

    public static final String NAME = "length";

    public LengthDriftMonitor(DriftBaselineProperties properties) {
        super(properties.getLength(), NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean record(String query) {
        if (query == null) {
            return false;
        }
        histogram.add(query.length());
        return true;
    }
}
