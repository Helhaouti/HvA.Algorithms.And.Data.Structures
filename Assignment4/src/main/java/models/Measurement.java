package models;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Digital representation of a KNMI measurement.
 *
 * @author ADS team
 * @author Hamza el Haouti
 * @author Rida Zeamari
 */
public class Measurement {
    private final static int FIELD_STN = 0;
    private final static int FIELD_YYMMDDDD = 1;
    private final static int FIELD_FG = 4;
    private final static int FIELD_FXX = 9;
    private final static int FIELD_TG = 11;
    private final static int FIELD_TN = 12;
    private final static int FIELD_TX = 14;
    private final static int FIELD_SQ = 18;
    private final static int FIELD_RH = 22;
    private final static int FIELD_RHX = 23;
    private final static int NUM_FIELDS = 24;

    private final Station station;      // col0, STN
    private final LocalDate date;       // col1, YYMMDDDD
    private double averageWindSpeed;    // col4, FG in m/s  from 0.1 m/s
    private double maxWindGust;         // col9, FXX in m/s  from 0.1 m/s
    private double averageTemperature;  // col11, TG in degC  from 0.1 degC
    private double minTemperature;      // col12, TN in degC  from 0.1 degC
    private double maxTemperature;      // col14, TX in degC  from 0.1 degC
    private double solarHours;          // col18, SQ in hours  from 0.1 h, -1 = < 0.05
    private double precipitation;       // col22, RH in mm  from 0.1 mm, -1 = < 0.05
    private double maxHourlyPrecipitation;   // col23, RHX in mm  from 0.1 mm, -1 = < 0.05

    public Measurement(Station station, int dateNumber) {
        this.station = station;
        this.date = LocalDate.of(dateNumber / 10000, (dateNumber / 100) % 100, dateNumber % 100);
    }

    private Measurement(MeasurementBuilder builder) {
        this.station = builder.station;
        this.date = builder.date;

        this.averageWindSpeed = builder.averageWindSpeed;
        this.maxWindGust = builder.maxWindGust;
        this.averageTemperature = builder.averageTemperature;
        this.minTemperature = builder.minTemperature;
        this.maxTemperature = builder.maxTemperature;
        this.solarHours = builder.solarHours;
        this.precipitation = builder.precipitation;
        this.maxHourlyPrecipitation = builder.maxHourlyPrecipitation;
    }

    /**
     * converts a text line into a new Measurement instance
     * processes columns # STN, YYYYMMDD, FG, FXX, TG, TN, TX, SQ, RH, RHX as per documentation in the text files
     * converts integer values to doubles as per unit of measure indicators
     * empty or corrupt values are replaced by Double.NaN
     * -1 values that indicate < 0.05 are replaced by 0.0
     *
     * @param textLine textLine
     * @param stations a map of Stations that can be accessed by station number STN
     * @return a new Measurement instance that records all data values of above quantities
     * null if the station number cannot be resolved,
     * or the record is incomplete or cannot be parsed
     */
    public static Measurement fromLine(String textLine, Map<Integer, Station> stations) {
        var fields = Arrays.stream(textLine.split(",")).map(String::trim).toArray(String[]::new);

        // Return null, if not enough data is available.
        if (fields.length < NUM_FIELDS) return null;

        final int stationNumber = Integer.parseInt(fields[FIELD_STN]);
        final int dateNumber = Integer.parseInt(fields[FIELD_YYMMDDDD]);

        final Station station = stations.get(stationNumber);

        return new MeasurementBuilder(station, dateNumber)
                .setAverageWindSpeed(convertToDouble(fields[FIELD_FG]))
                .setMaxWindGust(convertToDouble(fields[FIELD_FXX]))
                .setAverageTemperature(convertToDouble(fields[FIELD_TG]))
                .setMinTemperature(convertToDouble(fields[FIELD_TN]))
                .setMaxTemperature(convertToDouble(fields[FIELD_TX]))
                .setSolarHours(convertToDouble(fields[FIELD_SQ]))
                .setPrecipitation(convertToDouble(fields[FIELD_RH]))
                .setMaxHourlyPrecipitation(convertToDouble(fields[FIELD_RHX]))
                .build();
    }

    /**
     * Converts a string value with an integer to a floating point number (double).
     * <p>
     * It processes the integer into a floating point number by multiplying it with 0.1.
     * If the value of is -1, 0.0 will be returned.
     * <p>
     * When string is corrupted, Double.NaN will be returned.
     *
     * @param value A string with an integer.
     * @return value * 0.1
     * || 0.0, if value == -1
     * || Double.NaN, if value is corrupt.
     */
    public static double convertToDouble(String value) {
        try {
            var parsedValue = Integer.parseInt(value);
            if (parsedValue == -1) return 0.0;
            return parsedValue * 0.1;
        } catch (NumberFormatException exception) {
            return Double.NaN;
        }
    }

    public Station getStation() {
        return station;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getAverageWindSpeed() {
        return averageWindSpeed;
    }

    public void setAverageWindSpeed(double averageWindSpeed) {
        this.averageWindSpeed = averageWindSpeed;
    }

    public double getMaxWindGust() {
        return maxWindGust;
    }

    public void setMaxWindGust(double maxWindGust) {
        this.maxWindGust = maxWindGust;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getSolarHours() {
        return solarHours;
    }

    public void setSolarHours(double solarHours) {
        this.solarHours = solarHours;
    }

    public double getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(double precipitation) {
        this.precipitation = precipitation;
    }

    public double getMaxHourlyPrecipitation() {
        return maxHourlyPrecipitation;
    }

    public void setMaxHourlyPrecipitation(Double maxHourlyPrecipitation) {
        this.maxHourlyPrecipitation = maxHourlyPrecipitation;
    }

    /**
     * Determines equality, by assessing whether o is:
     * the same instance || of Type Measurement && has an equal station with an equal date.
     *
     * @param o Object to compare against this.
     * @return Whether the criteria for equality is met.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Measurement that)) return false;

        if (!getStation().equals(that.getStation())) return false;
        return getDate().equals(that.getDate());
    }

    /**
     * Generates hashcode with the Integer.hashCode() based on this' station and date.
     *
     * @return The generated hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getStation(), getDate());
    }

    /**
     * Class that allows for the creation of Measurement object with builder design pattern.
     * <p>
     * Also validates the provided data, and returns null, if any value is incomplete.
     */
    public static class MeasurementBuilder {
        private final Station station;
        private LocalDate date;
        private Double averageWindSpeed;
        private Double maxWindGust;
        private Double averageTemperature;
        private Double minTemperature;
        private Double maxTemperature;
        private Double solarHours;
        private Double precipitation;
        private Double maxHourlyPrecipitation;

        public MeasurementBuilder(Station station, int dateNumber) {
            this.station = station;

            try {
                this.date = LocalDate.of(dateNumber / 10000, (dateNumber / 100) % 100, dateNumber % 100);
            } catch (Exception e) {
                this.date = null;
            }
        }

        public MeasurementBuilder setAverageWindSpeed(double averageWindSpeed) {
            this.averageWindSpeed = averageWindSpeed;
            return this;
        }

        public MeasurementBuilder setMaxWindGust(double maxWindGust) {
            this.maxWindGust = maxWindGust;
            return this;
        }

        public MeasurementBuilder setAverageTemperature(double averageTemperature) {
            this.averageTemperature = averageTemperature;
            return this;
        }

        public MeasurementBuilder setMinTemperature(double minTemperature) {
            this.minTemperature = minTemperature;
            return this;
        }

        public MeasurementBuilder setMaxTemperature(double maxTemperature) {
            this.maxTemperature = maxTemperature;
            return this;
        }

        public MeasurementBuilder setSolarHours(double solarHours) {
            this.solarHours = solarHours;
            return this;
        }

        public MeasurementBuilder setPrecipitation(double precipitation) {
            this.precipitation = precipitation;
            return this;
        }

        public MeasurementBuilder setMaxHourlyPrecipitation(double maxHourlyPrecipitation) {
            this.maxHourlyPrecipitation = maxHourlyPrecipitation;
            return this;
        }

        /**
         * Builds and returns a Measurement object based on the provided data. If the provided data however
         * is incomplete, null will be returned.
         *
         * @return A Measurement object, or null, depending on the completeness of the provided data.
         */
        public Measurement build() {
            return this.isProvidedDataComplete()
                    ? new Measurement(this)
                    : null;
        }

        /**
         * Determines whether the builder contains all necessary data to create a Measurement object.
         *
         * @return Whether all necessary data is present.
         */
        private boolean isProvidedDataComplete() {
            if (this.station == null) return false;

            final var allValues = new Object[]{
                    date,
                    averageWindSpeed,
                    maxWindGust,
                    averageTemperature,
                    minTemperature,
                    maxTemperature,
                    solarHours,
                    precipitation,
                    maxHourlyPrecipitation
            };

            return Arrays.stream(allValues).noneMatch(Objects::isNull);
        }
    }
}
