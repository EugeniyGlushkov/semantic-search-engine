package ru.alvisid.semanticsearchengine.worker.drift;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author EGlushkov
 * Date: 09.09.2026
 * Time: 17:53
 */

/*
 * Гистограмма для мониторинга распределения признака.
 * Хранит границы бинов и счётчики наблюдений в каждом бине.
 * Умеет вычислять PSI относительно эталонной гистограммы.
 */
@Slf4j
public class Histogram {
    // Малое значение для сглаживания нулевых долей (избегаем ln(0) и деления на 0).
    private static final double EPSILON = 0.0001;
    // Если наблюдений меньше порога, то считаем, что отклонений нет. PSI не вычисляем.
    public static final long MIN_OBSERVATIONS_FOR_PSI = 100;

    // Границы бинов (верхние границы). Например, для длины запроса: [10, 20, 50, 100, Double.MAX_VALUE]
    private final double[] binEdges;
    // Счётчики наблюдений в каждом бине
    private final AtomicLongArray counters;

    /**
     * Конструктор гистограммы.
     * <p>Количество бинов равно количеству границ. Последняя граница — верхняя граница
     * последнего бина; все значения больше неё попадают в последний бин.
     * <p>Для корректной работы рекомендуется указывать {@code Double.MAX_VALUE}
     * в качестве последней границы, чтобы покрыть весь диапазон.
     *
     * @param binEdges массив верхних границ бинов (отсортирован по возрастанию)
     */
    public Histogram(double[] binEdges) {
        if (binEdges == null || binEdges.length == 0) {
            throw new IllegalArgumentException("binEdges must not be empty");
        }
        for (int i = 1; i < binEdges.length; i++) {
            if (binEdges[i] <= binEdges[i-1]) {
                throw new IllegalArgumentException("binEdges must be strictly increasing");
            }
        }
        this.binEdges = Arrays.copyOf(binEdges, binEdges.length);
        this.counters = new AtomicLongArray(new long[binEdges.length]);
    }

    /**
     * Добавляет наблюдение.
     *
     * @param value значение признака
     */
    public void add(double value) {
        if (!Double.isFinite(value)) {
            log.warn("Добавляемое значение должно быть числом: {}", value);
            return;
        }

        int index = findBin(value);
        if (index < 0 || index >= counters.length()) {
            log.warn("Значение {} не попадает ни в один бин", value);
            return;
        }
        counters.incrementAndGet(index);
    }

    /**
     * Определяет индекс бина для заданного значения.
     *
     * @param value значение
     * @return индекс бина (0 .. binEdges.length-1)
     */
    private int findBin(double value) {
        int idx = Arrays.binarySearch(binEdges, value);

        if (idx < 0) {
            // Значение не найдено. binarySearch возвращает (-insertionPoint - 1),
            // где insertionPoint — индекс, куда значение было бы вставлено.
            // Нам нужен именно этот insertionPoint, потому что он указывает на первый бин,
            // верхняя граница которого >= value.
            idx = -idx - 1;
        }
        // Если value больше всех границ (например, из-за отсутствия MAX_VALUE в конце),
        // относим его к последнему бину.
        if (idx >= binEdges.length) {
            idx = binEdges.length - 1;
        }
        return idx;

    }

    /**
     * Возвращает доли (пропорции) наблюдений в каждом бине.
     * Сумма долей = 1.0.
     */
    public ProportionsSnapshot getProportions() {
        long[] currentCounts = getCounts(); //make count's snapshot
        long localTotal = Arrays.stream(currentCounts).sum();

        double[] result = new double[currentCounts.length];
        if (localTotal == 0) {
            return new ProportionsSnapshot(result, localTotal);
        }

        for (int i = 0; i < currentCounts.length; i++) {
            result[i] = (double) currentCounts[i] / localTotal;
        }

        return new ProportionsSnapshot(result, localTotal);
    }

    /**
     * Возвращает сырые счётчики наблюдений в каждом бине.
     */
    public long[] getCounts() {
        long[] result = new long[counters.length()];
        for (int i = 0; i < counters.length(); i++) {
            result[i] = counters.get(i);
        }
        return result;
    }

    /**
     * Общее количество наблюдений.
     */
    public long getTotalCount() {
        return Arrays.stream(getCounts()).sum();
    }

    /**
     * Вычисляет PSI (Population Stability Index) относительно эталонной гистограммы.
     *
     * @param expected эталонная гистограмма (бейзлайн)
     * @return значение PSI
     */
    public DriftSnapshot computePSI(double[] expected) {
        if (expected == null) {
            throw new IllegalArgumentException("Expected baseline must not be null");
        }

        if (expected.length != this.binEdges.length) {
            throw new IllegalArgumentException("Количество бинов должно совпадать");
        }

        ProportionsSnapshot proportionsAndTotal = getProportions();
        double[] actual = proportionsAndTotal.proportions();
        long snapshotTotal = proportionsAndTotal.total();


        // Если данных мало — считаем, что дрейфа нет (безопасное значение по умолчанию)
        if (snapshotTotal < MIN_OBSERVATIONS_FOR_PSI) {
            return new DriftSnapshot(snapshotTotal, Double.NaN);
        }

        double psi = 0.0;

        for (int i = 0; i < actual.length; i++) {
            double actualSmoothed = Math.max(actual[i], EPSILON);
            double expectedSmoothed = Math.max(expected[i], EPSILON);
            double diff = actualSmoothed - expectedSmoothed;
            double ratio = actualSmoothed / expectedSmoothed;
            psi += diff * Math.log(ratio);
        }

        return new DriftSnapshot(snapshotTotal, psi);
    }

    /**
     * Сбрасывает все счётчики (для начала нового периода).
     */
    public void reset() {
        for (int i = 0; i < counters.length(); i++) {
            counters.set(i, 0);
        }
    }

    /**
     * Возвращает копию границ бинов.
     */
    public double[] getBinEdges() {
        return Arrays.copyOf(binEdges, binEdges.length);
    }

    @Override
    public String toString() {
        return "Histogram{" +
                "binEdges=" + Arrays.toString(binEdges) +
                ", counts=" + Arrays.toString(getCounts()) +
                ", proportions=" + Arrays.toString(getProportions().proportions()) +
                '}';
    }
}