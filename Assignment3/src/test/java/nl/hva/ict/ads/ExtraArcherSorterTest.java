package nl.hva.ict.ads;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Test class intended for the development of performance tests.
 *
 * @author Hamza el Haouti
 */
public class ExtraArcherSorterTest extends ArcherSorterTest {
    private final static int INITIAL_LIST_SIZE = 100;
    private final static int NUMBER_OF_ROUNDS = 10;
    private final static int TOP_SIZE_HEAP_SORT = 25;

    private final static int MAX_AMOUNT_OF_ARCHERS_SORTED = 5000000;
    private final static long MAX_TEST_DURATION_NANOSECONDS = (long) (20 * Math.pow(10, 9));

    private static final String SEL_INS_SORT_NAME = "selInsSort";
    private static final String QUICK_SORT_NAME = "quickSort";
    private static final String TOTAL_HEAP_SORT_NAME = "totalHeapSort";
    private static final String TOPS_HEAP_SORT_NAME = "topsHeapSort";

    /**
     * We started of with an amount of items equals to INITIAL_LIST_SIZE. We then multiplied this by 2, for each
     * following iteration.
     * <p>
     * An iteration consist of a number of round equal to NUMBER_OF_ROUNDS performance measurement of 3 algorithms,
     * with Heap sort having been tasked to sort both top items equal to TOP_SIZE_HEAP_SORT and the
     * entire list. Each measurement is based on the same randomly generated and ordered list with archers. From which
     * an average is drawn for the iteration.
     * <p>
     * The test is then deemed complete when the to be sorted list has size equal to MAX_AMOUNT_OF_ARCHERS_SORTED
     * or takes longer than MAX_TEST_DURATION_NANOSECONDS to sort.
     * <p>
     * Import side-notes:
     * After each generation of an input set it utilizes System.gc() (Java garbage collection)
     * to remove any unused objects in memory.
     * <p>
     * Use JVM -Xint to disable Java JIT Compiler, to prevent L3 caching of objects.
     */
    @Test
    void measureSortingSpeed() {
        printSortingMeasurements(measureSortingSpeed(Map.of()));
    }

    /**
     * @return A map with Key:algorithm name and Value: a list with the time duration of each sorting attempt.
     */
    private static Map<String, List<Double>> measureSortingSpeed(Map<String, List<Double>> stats) {
        Map<String, List<Double>> statistics = new HashMap<>(stats);
        var sorter = new ArcherSorter();
        Comparator<Archer> comparator = Archer::compareByHighestTotalScoreWithLeastMissesAndLowestId;

        AtomicReference<Boolean> doAnotherRoundOfMeasurements = new AtomicReference<>(false);

        var scoresOfSelInsSort = convertToArrayList(statistics.get(SEL_INS_SORT_NAME));
        var scoresOfQuickSort = convertToArrayList(statistics.get(QUICK_SORT_NAME));
        var scoresOfTotalHeapSort = convertToArrayList(statistics.get(TOTAL_HEAP_SORT_NAME));
        var scoresOfTopsHeapSort = convertToArrayList(statistics.get(TOPS_HEAP_SORT_NAME));

        int largestNumberOfRounds = Integer.max(
                Integer.max(scoresOfSelInsSort.size(), scoresOfQuickSort.size()),
                scoresOfTopsHeapSort.size());

        var attemptDurationOfSelInsSort = new ArrayList<Long>();
        var attemptDurationOfQuickSort = new ArrayList<Long>();
        var attemptDurationOfTotalHeapSort = new ArrayList<Long>();
        var attemptDurationOfTopsHeapSort = new ArrayList<Long>();

        for (int i = 0; i < NUMBER_OF_ROUNDS; i++) {
            List<Archer> archerList = generateArcherList(determineListSize(largestNumberOfRounds));

            runSorting(scoresOfSelInsSort,
                    archerList,
                    comparator,
                    sorter::selInsSort,
                    doAnotherRoundOfMeasurements::set,
                    attemptDurationOfSelInsSort::add);

            runSorting(scoresOfQuickSort,
                    archerList,
                    comparator,
                    sorter::quickSort,
                    doAnotherRoundOfMeasurements::set,
                    attemptDurationOfQuickSort::add);

            runSorting(scoresOfTotalHeapSort,
                    archerList,
                    comparator,
                    (items, comparator1) -> sorter.topsHeapSort(archerList.size(), items, comparator1),
                    doAnotherRoundOfMeasurements::set,
                    attemptDurationOfTotalHeapSort::add);

            runSorting(scoresOfTopsHeapSort,
                    archerList,
                    comparator,
                    (items, comparator1) -> sorter.topsHeapSort(TOP_SIZE_HEAP_SORT, items, comparator1),
                    doAnotherRoundOfMeasurements::set,
                    attemptDurationOfTopsHeapSort::add);
        }

        if (!attemptDurationOfSelInsSort.isEmpty())
            scoresOfSelInsSort.add(calculateAverage(attemptDurationOfSelInsSort));
        if (!attemptDurationOfQuickSort.isEmpty())
            scoresOfQuickSort.add(calculateAverage(attemptDurationOfQuickSort));
        if (!attemptDurationOfTotalHeapSort.isEmpty())
            scoresOfTotalHeapSort.add(calculateAverage(attemptDurationOfTotalHeapSort));
        if (!attemptDurationOfTopsHeapSort.isEmpty())
            scoresOfTopsHeapSort.add(calculateAverage(attemptDurationOfTopsHeapSort));

        statistics = Map.of(SEL_INS_SORT_NAME, scoresOfSelInsSort,
                QUICK_SORT_NAME, scoresOfQuickSort,
                TOTAL_HEAP_SORT_NAME, scoresOfTotalHeapSort,
                TOPS_HEAP_SORT_NAME, scoresOfTopsHeapSort);

        return doAnotherRoundOfMeasurements.get() ? measureSortingSpeed(statistics) : statistics;
    }

    private static List<Archer> generateArcherList(int listSize) {
        var championSelector = new ChampionSelector(System.nanoTime());
        var generatedArchers = new ArrayList<>(championSelector.enrollArchers(listSize));
        Collections.shuffle(generatedArchers);
        return generatedArchers;
    }

    private static <E> ArrayList<E> convertToArrayList(List<E> list) {
        return new ArrayList<>(Objects.requireNonNullElse(list, List.of()));
    }

    private static void runSorting(List<Double> previousRuns,
                                   List<Archer> toBeSorted,
                                   Comparator<Archer> comparator,
                                   BiFunction<List<Archer>, Comparator<Archer>, List<Archer>> sorter,
                                   Consumer<Boolean> doAnotherRoundOfMeasurements,
                                   Consumer<Long> durationOfAttempt
    ) {
        if (determineListSize(previousRuns.size()) > MAX_AMOUNT_OF_ARCHERS_SORTED
                || !isLastOf(previousRuns, x -> x <= MAX_TEST_DURATION_NANOSECONDS)) return;
        var toBeSortedCopy = convertToArrayList(List.copyOf(toBeSorted));
        System.gc();

        long start = System.nanoTime();
        sorter.apply(toBeSortedCopy, comparator);
        durationOfAttempt.accept(System.nanoTime() - start);

        doAnotherRoundOfMeasurements.accept(true);
    }

    private static double calculateAverage(List<Long> numbers) {
        return numbers.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(Double.NaN);
    }

    private static boolean isLastOf(List<Double> list, Predicate<Double> condition) {
        return list.isEmpty()
                || condition.test(list.get(list.size() - 1));
    }

    private static int determineListSize(int rounds) {
        return (int) (INITIAL_LIST_SIZE * Math.pow(2, rounds));
    }

    private static void printSortingMeasurements(Map<String, List<Double>> stats) {
        System.out.println("\nComparing various search implementations (msec)");

        final String PRINT_FORMAT = "\n%-8s %-20s %-20s %-20s %-20s";

        System.out.printf(PRINT_FORMAT,
                "N",
                "T(" + SEL_INS_SORT_NAME + ")",
                "T(" + QUICK_SORT_NAME + ")",
                "T(" + TOPS_HEAP_SORT_NAME + ")",
                "T(" + TOTAL_HEAP_SORT_NAME + ")");

        var measurementsOfSelInsSort = stats.get(SEL_INS_SORT_NAME);
        var measurementsOfQuickSort = stats.get(QUICK_SORT_NAME);
        var measurementsOfTopsHeapSort = stats.get(TOPS_HEAP_SORT_NAME);
        var measurementsOfTotalHeapSort = stats.get(TOTAL_HEAP_SORT_NAME);

        int largestListSize = Integer.max(measurementsOfTopsHeapSort.size(),
                Integer.max(measurementsOfSelInsSort.size(), measurementsOfQuickSort.size()));

        for (int i = 0; i < largestListSize; i++)
            System.out.printf(PRINT_FORMAT,
                    determineListSize(i),
                    measurementsOfSelInsSort.size() > i ? measurementsOfSelInsSort.get(i) * 1E-6 : "",
                    measurementsOfQuickSort.size() > i ? measurementsOfQuickSort.get(i) * 1E-6 : "",
                    measurementsOfTopsHeapSort.size() > i ? measurementsOfTopsHeapSort.get(i) * 1E-6 : "",
                    measurementsOfTotalHeapSort.size() > i ? measurementsOfTotalHeapSort.get(i) * 1E-6 : "");
    }
}