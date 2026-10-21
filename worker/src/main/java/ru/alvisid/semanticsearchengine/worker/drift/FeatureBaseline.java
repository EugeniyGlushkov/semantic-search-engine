package ru.alvisid.semanticsearchengine.worker.drift;

import lombok.Data;

/**
 * @author EGlushkov
 * Date: 27.09.2026
 * Time: 21:44
 */

@Data
public class FeatureBaseline {
    /** Верхние границы бинов (отсортированы по возрастанию). */
    private double[] bins;
    /** Эталонные доли для каждого бина (сумма = 1.0). */
    private double[] proportions;
}