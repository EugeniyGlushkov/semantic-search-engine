package ru.alvisid.semanticsearchengine.worker.drift.monitors;

import lombok.extern.slf4j.Slf4j;
import ru.alvisid.semanticsearchengine.worker.drift.DriftSnapshot;
import ru.alvisid.semanticsearchengine.worker.drift.FeatureBaseline;
import ru.alvisid.semanticsearchengine.worker.drift.Histogram;

/**
 * @author EGlushkov
 * Date: 28.09.2026
 * Time: 11:12
 */

@Slf4j
public abstract class AbstractFeatureDriftMonitor implements FeatureDriftMonitor {

    protected final Histogram histogram;
    protected final double[] baselineProportions;

    protected AbstractFeatureDriftMonitor(FeatureBaseline baseline, String featureName) {
        if (baseline == null) {
            throw new IllegalStateException("Baseline for feature '" + featureName + "' must be configured");
        }
        if (baseline.getBins() == null || baseline.getProportions() == null) {
            throw new IllegalStateException("Baseline for '" + featureName + "' must have bins and proportions");
        }
        if (baseline.getBins().length != baseline.getProportions().length) {
            throw new IllegalStateException("Bins and proportions count must match for '" + featureName + "'");
        }
        this.histogram = new Histogram(baseline.getBins());
        this.baselineProportions = baseline.getProportions();
        log.info("FeatureDriftMonitor '{}' инициализирован: {} бинов", featureName, baseline.getBins().length);
    }

    @Override
    public DriftSnapshot computePSI() {
        return histogram.computePSI(baselineProportions);
    }

    @Override
    public long getTotalCount() {
        return histogram.getTotalCount();
    }

    @Override
    public void reset() {
        histogram.reset();
    }
}
