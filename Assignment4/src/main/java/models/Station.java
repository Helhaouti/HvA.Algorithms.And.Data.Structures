package models;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Digital representation of a Station object.
 *
 * @author ADS Team.
 * @author Rida Zeamari
 * @author Hamza el Haouti
 */
public class Station {
    private final int stn;
    private final String name;
    private final NavigableMap<LocalDate, Measurement> measurements;

    public Station(int id, String name) {
        this.stn = id;
        this.name = name;
        this.measurements = new TreeMap<>();
    }

    /**
     * Import station number and name from a text line
     *
     * @param textLine A string with two comma seperated values, with format: "<Station number>, <Station name>"
     * @return A new Station instance for this data
     * or null if the data format does not comply
     */
    public static Station fromLine(String textLine) {
        try {
            String[] fields = textLine.split(",");
            return new Station(Integer.parseInt(fields[0].trim()), fields[1].trim());
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Add a collection of new measurements to this station.
     * Measurements that are not related to this station
     * and measurements with a duplicate date shall be ignored and not added
     *
     * @param newMeasurements New measurements
     * @return The nett number of measurements which have been added.
     */
    public int addMeasurements(Collection<Measurement> newMeasurements) {
        int oldSize = this.getMeasurements().size();

        this.measurements.putAll(newMeasurements
                .stream()
                .collect(Collectors.toMap(Measurement::getDate, p -> p))
        );

        return this.getMeasurements().size() - oldSize;
    }

    /**
     * Calculates the all-time maximum temperature for this station
     *
     * @return The maximum temperature ever measured at this station
     *          returns Double.NaN when no valid measurements are available
     */
    public double allTimeMaxTemperature() {
        return getMeasurements().stream()
                .mapToDouble(Measurement::getMaxTemperature)
                .filter(m -> !Double.isNaN(m))
                .max()
                .orElse(Double.NaN);
    }

    /**
     * @return The date of the first day of a measurement for this station
     *          returns Optional.empty() if no measurements are available
     */
    public Optional<LocalDate> firstDayOfMeasurement() {
        return Optional.of(measurements.firstEntry().getKey());
    }

    /**
     * Calculates the number of valid values of the data field that is specified by the mapper
     * invalid or empty values should be represented by Double.NaN
     * this method can be used to check on different types of measurements each with their own mapper
     *
     * @param mapper The getter method of the data field to be checked.
     * @return The number of valid values found
     */
    public int numValidValues(Function<Measurement, Double> mapper) {
        return (int) getMeasurements().stream()
                .map(mapper)
                .filter(m -> !Double.isNaN(m))
                .count();
    }

    /**
     * Calculates the total precipitation at this station
     * across the time period between startDate and endDate (inclusive)
     *
     * @param startDate The start date of the period of accumulation (inclusive)
     * @param endDate   The end date of the period of accumulation (inclusive)
     * @return The total precipitation value across the period
     *                      0.0 if no measurements have been made in this period.
     */
    public double totalPrecipitationBetween(LocalDate startDate, LocalDate endDate) {
        return measurements.subMap(startDate, true, endDate, true).values()
                .stream()
                .mapToDouble(Measurement::getPrecipitation)
                .filter(d -> !Double.isNaN(d))
                .sum();
    }

    /**
     * Calculates the average of all valid measurements of the quantity selected by the mapper function
     * across the time period between startDate and endDate (inclusive)
     *
     * @param startDate The start date of the period of averaging (inclusive)
     * @param endDate   The end date of the period of averaging (inclusive)
     * @param mapper    A getter method that obtains the double value from a measurement instance to be averaged
     * @return The average of all valid values of the selected quantity across the period
     * Double.NaN if no valid measurements are available from this period.
     */
    public double averageBetween(
            LocalDate startDate,
            LocalDate endDate,
            Function<Measurement, Double> mapper
    ) {
        return measurements.subMap(startDate, endDate).values()
                .stream()
                .mapToDouble(mapper::apply)
                .average().orElse(Double.NaN);
    }

    /**
     * Returns an unmodifiable copy of all performed measurements.
     *
     * @return an unmodifiable copy of all performed measurements.
     */
    public Collection<Measurement> getMeasurements() {
        return Set.copyOf(measurements.values());
    }

    public int getStn() {
        return stn;
    }

    public String getName() {
        return name;
    }

    /**
     * Determines equality, by assessing whether o is:
     * the same instance || of Type Station && has the same station number.
     *
     * @param o Object to compare against this.
     * @return Whether the criteria for equality is met.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station station)) return false;
        return getStn() == station.getStn();
    }

    /**
     * Generates hashcode with the Integer.hashCode() based on this' station number.
     *
     * @return The generated hashcode.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(getStn());
    }

    /**
     * Generates a string representation of this, with format: "<station number>/<station name>"
     *
     * @return A string representation of this.
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d/%s",
                this.getStn(),
                this.getName()
        );
    }
}