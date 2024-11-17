import models.ClimateTracker;
import models.Measurement;
import models.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClimateTrackerTest {

    ClimateTracker climateTracker;

    @BeforeEach
    private void setup() {
        climateTracker = new ClimateTracker();

        climateTracker.importClimateDataFromVault(ClimateTracker.class.getResource("/test").getPath());
    }

    @Test
    public void importVaultCheck() {
        assertEquals(5, climateTracker.getStations().size());

        assertThat(climateTracker.numberOfMeasurementsByStation().keySet(), equalTo(new HashSet<>(climateTracker.getStations())));
        assertThat(climateTracker.numberOfMeasurementsByStation().values(), containsInAnyOrder(609,5,0,0,488));
        assertThat(climateTracker.numberOfValidValuesByStation(Measurement::getMaxWindGust).values(), containsInAnyOrder(609,4,0,0,488));
        assertThat(climateTracker.numberOfValidValuesByStation(Measurement::getAverageTemperature).values(), containsInAnyOrder(609,2,0,0,488));
        assertThat(climateTracker.numberOfValidValuesByStation(Measurement::getSolarHours).values(), containsInAnyOrder(609,3,0,0,488));
        assertThat(climateTracker.numberOfValidValuesByStation(Measurement::getMaxHourlyPrecipitation).values(), containsInAnyOrder(609,2,0,0,488));
    }

    @Test
    public void importVaultDuplicatesCheck() {
        assertThrows(InputMismatchException.class,
                () -> climateTracker.importMeasurementsFromVault(ClimateTracker.class.getResource("/test/measurements").getPath())
        );
    }

    @Test
    public void checkStationAccess() {
        assertTrue(climateTracker.getStations().size() > 0);
        for (Station station : climateTracker.getStations()) {
            assertSame(station, climateTracker.findStationById(station.getStn()));
        }

        Station station = climateTracker.findStationById(99999);
        assertNull(station, "Finding a non-existing station returns null");
    }

    @Test void checkBetweenValues() {
        Station vlissingen = climateTracker.findStationById(310);
        assertEquals(14.2, vlissingen.totalPrecipitationBetween(LocalDate.of(2021,11,2), LocalDate.of(2021,11,4)), 0.0001);
        assertEquals(0, vlissingen.totalPrecipitationBetween(LocalDate.of(2021,11,1), LocalDate.of(2021,11,2)), 0.0001);
        assertEquals(9.5, vlissingen.averageBetween(LocalDate.of(2021,11,1), LocalDate.of(2021,11,3), Measurement::getAverageTemperature), 0.0001);
        assertTrue(Double.isNaN(vlissingen.averageBetween(LocalDate.of(2021,11,3), LocalDate.of(20211,11,4), Measurement::getAverageTemperature)));
    }

    @Test void checkFirstDates() {
        assertEquals("{260/De Bilt=2019-01-01, 310/Vlissingen=2021-11-01, 380/Maastricht=2019-04-01}",
                climateTracker.firstDayOfMeasurementByStation().toString());
    }

    @Test void checkTrends() {
        Map<Integer,Double> averageTempTrend = climateTracker.annualAverageTemperatureTrend();
        //System.out.println(averageTempTrend);
        assertArrayEquals(new double[] {13.5198, 12.8538, 9.5},
                averageTempTrend.values().stream().mapToDouble(v->v).toArray(),
                0.01);

        Map<Integer,Double> maxHourlyPrecipitationTrend = climateTracker.annualMaximumTrend(Measurement::getMaxHourlyPrecipitation);
        //System.out.println(maxHourlyPrecipitationTrend);
        assertArrayEquals(new double[] {24.7, 21.0, 1.6},
                maxHourlyPrecipitationTrend.values().stream().mapToDouble(v->v).toArray(),
                0.01);
    }

    @Test void checkProfiles() {
        //System.out.println(climateTracker.allTimeMonthlyMeasurementCount(Measurement::getSolarHours));

        Map<Month,Double> solarProfile = climateTracker.allTimeAverageDailySolarByMonth();
        //System.out.println(solarProfile);
        assertArrayEquals(new double[] {1.6505, 3.2581, 5.1946, 8.6866, 8.5104, 8.3825, 7.2330, 7.3717, 6.1958, 2.9247, 2.4},
                solarProfile.values().stream().mapToDouble(v->v).toArray(),
                0.01);

        assertEquals(2020, climateTracker.coldestYear());
    }
}
