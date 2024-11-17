import models.ClimateTracker;
import models.Measurement;
import models.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraTest {

    @BeforeEach
    private void setup() {

    }

    @Test
    public void checkIfClimateTrackerProducesExpectedValues() {
        final var DELTA = 0.0009;

        ClimateTracker climateTracker = new ClimateTracker();
        climateTracker.importClimateDataFromVault(ClimateAnalysisMain.class.getResource("/knmi").getPath());

        Station deBilt = Station.fromLine("260, De Bilt");
        Station vlissingen = Station.fromLine("310, Vlissingen");
        Station eelde = Station.fromLine("280, Eelde");
        Station deKooy = Station.fromLine("235, De Kooy");
        Station maastricht = Station.fromLine("380, Maastricht");


        // Test: 1. Total Number of measurements by station
        var test1 = climateTracker.numberOfMeasurementsByStation().entrySet();
        var correctValues1 = List.of(
                Map.entry(deBilt, 44152),
                Map.entry(vlissingen, 42326),
                Map.entry(eelde, 42326),
                Map.entry(deKooy, 42326),
                Map.entry(maastricht, 42326)
        );

        assertArrayEquals(correctValues1.toArray(), test1.toArray());

        // Test: 2. First day of measurement by station
        var test2 = climateTracker.firstDayOfMeasurementByStation().entrySet();
        var correctValues2 = List.of(
                Map.entry(deBilt, LocalDate.of(1901, 1, 1)),
                Map.entry(vlissingen, LocalDate.of(1906, 1, 1)),
                Map.entry(eelde, LocalDate.of(1906, 1, 1)),
                Map.entry(deKooy, LocalDate.of(1906, 1, 1)),
                Map.entry(maastricht, LocalDate.of(1906, 1, 1))
        );

        assertArrayEquals(correctValues2.toArray(), test2.toArray());

        // Test:  3. All-time maximum temperature in de Bilt degC
        var test3 = climateTracker.findStationById(260).allTimeMaxTemperature();
        assertEquals(37.5, test3);


        // Test:  4. Number of valid daily precipitation measurements by station
        var test4 = climateTracker.numberOfValidValuesByStation(Measurement::getPrecipitation).entrySet();
        var correctValues4 = List.of(
                Map.entry(deBilt, 42296),
                Map.entry(vlissingen, 23715),
                Map.entry(eelde, 23698),
                Map.entry(deKooy, 23698),
                Map.entry(maastricht, 23698)
        );

        // Compare order;
        assertArrayEquals(correctValues4.toArray(), test4.toArray());

        // Compare values;
        assertArrayEquals(
                correctValues4.stream().mapToDouble(Map.Entry::getValue).toArray(),
                test4.stream().mapToDouble(Map.Entry::getValue).toArray(),
                DELTA
        );


        // Test: 5. Total precipitation in de Bilt in 1963 = %.0f mm
        var test5 = climateTracker.findStationById(260).totalPrecipitationBetween(LocalDate.of(1963, 1, 1), LocalDate.of(1963, 12, 31));
        assertEquals(777, Math.ceil(test5));

        // Test: 6. Annual trend of average temperatures (in degC)
        var correctValues6 = Map.ofEntries(
                Map.entry(2021, 11.151987577639751),
                Map.entry(1910, 9.324383561643836),
                Map.entry(1909, 8.191123287671234),
                Map.entry(1908, 8.40584699453552),
                Map.entry(1907, 8.674849315068494),
                Map.entry(1906, 9.463703284258212),
                Map.entry(1905, 8.713698630136987),
                Map.entry(1904, 8.93360655737705),
                Map.entry(1903, 9.166575342465753),
                Map.entry(1902, 8.245205479452055),
                Map.entry(1901, 8.783561643835617)
        );
        var sortedCorrectValues6 = correctValues6.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        var test6 = climateTracker.annualAverageTemperatureTrend().entrySet()
                .stream()
                .filter(v -> correctValues6.containsKey(v.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Compare order;
        assertArrayEquals(
                sortedCorrectValues6.stream().map(Map.Entry::getKey).toArray(),
                test6.keySet().toArray()
        );
        // Compare values;
        assertArrayEquals(
                sortedCorrectValues6.stream().mapToDouble(Map.Entry::getValue).toArray(),
                test6.values().stream().mapToDouble(Double::doubleValue).toArray(),
                DELTA
        );

        // Test: 7. Annual trend of maximum hourly precipitation (in mm)
        var correctValues7 = Map.ofEntries(
                Map.entry(1906, 13.200000000000001),
                Map.entry(1907, 9.200000000000001),
                Map.entry(1908, 11.100000000000001),
                Map.entry(1909, 28.200000000000003),
                Map.entry(1910, 23.400000000000002),
                Map.entry(1911, 16.400000000000002),
                Map.entry(1912, 17.900000000000002),
                Map.entry(1913, 15.3),
                Map.entry(1914, 13.700000000000001),
                Map.entry(1915, 10.700000000000001),
                Map.entry(2020, 51.300000000000004),
                Map.entry(2021, 67.0)
        );
        var sortedCorrectValues7 = correctValues7.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        var test7 = climateTracker.annualMaximumTrend(Measurement::getMaxHourlyPrecipitation).entrySet()
                .stream()
                .filter(v -> correctValues7.containsKey(v.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Compare order;
        assertArrayEquals(
                sortedCorrectValues7.stream().map(Map.Entry::getKey).toArray(),
                test7.keySet().toArray()
        );
        // Compare values;
        assertArrayEquals(
                sortedCorrectValues7.stream().mapToDouble(Map.Entry::getValue).toArray(),
                test7.values().stream().mapToDouble(Double::doubleValue).toArray(),
                DELTA
        );

        // Test: 8. Annual trend of maximum wind gust (in m/s)
        var correctValues8 = Map.ofEntries(
                Map.entry(1951, 25.7),
                Map.entry(1952, 22.6),
                Map.entry(1953, 33.4),
                Map.entry(1954, 36.0),
                Map.entry(2016, 35.0),
                Map.entry(2017, 31.0),
                Map.entry(2018, 39.0),
                Map.entry(2019, 34.0),
                Map.entry(2020, 33.0),
                Map.entry(2021, 33.0)
        );
        var sortedCorrectValues8 = correctValues8.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        var test8 = climateTracker.annualMaximumTrend(Measurement::getMaxWindGust).entrySet()
                .stream()
                .filter(v -> correctValues8.containsKey(v.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Compare order;
        assertArrayEquals(
                sortedCorrectValues8.stream().map(Map.Entry::getKey).toArray(),
                test8.keySet().toArray()
        );
        // Compare values;
        assertArrayEquals(
                sortedCorrectValues8.stream().mapToDouble(Map.Entry::getValue).toArray(),
                test8.values().stream().mapToDouble(Double::doubleValue).toArray(),
                DELTA
        );

        // Test: 9. All-time monthly profile of daily solar hours
        var test9 = climateTracker.allTimeAverageDailySolarByMonth().entrySet();
        var correctValues9 = List.of(
                Map.entry(Month.of(1), 1.7026519583263693),
                Map.entry(Month.of(2), 2.6604576967912146),
                Map.entry(Month.of(3), 3.9071492354057376),
                Map.entry(Month.of(4), 5.574926338898839),
                Map.entry(Month.of(5), 6.822385310860587),
                Map.entry(Month.of(6), 6.862594277160459),
                Map.entry(Month.of(7), 6.451513802315227),
                Map.entry(Month.of(8), 6.111598467603131),
                Map.entry(Month.of(9), 4.852380952380952),
                Map.entry(Month.of(10), 3.3691713014460514),
                Map.entry(Month.of(11), 1.9094002306805076),
                Map.entry(Month.of(12), 1.3689705469845723)
        );

        // Compare order.
        assertArrayEquals(
                correctValues9.toArray(),
                test9.toArray()
        );

        // Test: 10. Coldest year
        var coldestYear = climateTracker.coldestYear();
        assertEquals(coldestYear, 1963);
    }
}
