import models.Station;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StationTest {

    Station deBilt, vlissingen;

    @BeforeEach
    private void setup() {
        deBilt = new Station(260, "De Bilt");
        vlissingen = new Station(310, "Vlissingen");
    }

    @Test
    public void aStationHasAStringRepresentation() {
        assertEquals("260/De Bilt", deBilt.toString());
    }

    @Test
    public void testEquality() {
        Station s1 = Station.fromLine("1,Amsterdam");
        Station s2 = Station.fromLine("1,Amsterdam");
        Station s3 = Station.fromLine("1,Rotterdam");
        assertNotSame(s1, s2, "Different instances");
        assertEquals(s1, s2, "Equal values");
        assertEquals(s1, s3, "Equal (unique) station numbers");
        assertEquals(s1.hashCode(), s2.hashCode(),"Hashcode should be equal for equal values");
        assertEquals(s1.hashCode(), s3.hashCode(),"Hashcode should be equal for equal station numbers");
    }

    @Test
    public void canConvertATextLineToAStation() {
        Station station1 = Station.fromLine("235, De Kooy");
        Station station2 = Station.fromLine(" 380 , Maastricht , Nederland");
        Station station3 = Station.fromLine(" 999 ");

        assertEquals(235, station1.getStn());
        assertEquals("De Kooy", station1.getName());
        assertEquals(380, station2.getStn());
        assertEquals("Maastricht", station2.getName());
        assertEquals(null, station3);
    }

    @Test void allTimeMaxTemperatureReturnsNaNOnEmptyStation() {
        double allTimeMaxTemp = this.deBilt.allTimeMaxTemperature();
        assertTrue(Double.isNaN(allTimeMaxTemp), "An empty station should return NaN on allTimeMaxTemperature");
    }
}