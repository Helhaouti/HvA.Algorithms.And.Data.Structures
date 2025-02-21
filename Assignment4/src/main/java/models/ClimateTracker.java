package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author ADS team
 * @author Hamza el Haouti
 * @author Rida Zeamari
 */
public class ClimateTracker {
    private final String MEASUREMENTS_FILE_PATTERN = ".*\\.txt";

    /**
     * all available weather stations organised by Station Number (STN)
     */
    private Map<Integer, Station> stations;

    public ClimateTracker() {
        this.stations = new HashMap<>();
    }

    public Set<Station> getStations() {
        return Set.copyOf(this.stations.values());
    }

    public Station findStationById(int stn) {
        return stations.getOrDefault(stn, null);
    }

    /**
     * Calculates for each station how many Measurement instances have been registered
     *
     * @return A map(S, A) that provides for each station (S), the amount (A) of measurements.
     */
    public Map<Station, Integer> numberOfMeasurementsByStation() {
        return this.getStations()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                station -> station.getMeasurements().size()
                        )
                );
    }

    /**
     * Calculates for each station the date of the first day of its measurements
     * stations without measurements shall be excluded from the result
     *
     * @return A map(S, D) that provides for each station (S), the first day (D) of measurements.
     */
    public Map<Station, LocalDate> firstDayOfMeasurementByStation() {
        return this.getStations()
                .stream()
                .filter(station -> !station.getMeasurements().isEmpty())
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                // The filter mitigates the need for an Optional if present check.
                                station -> station.firstDayOfMeasurement().get()
                        )
                );
    }

    /**
     * Calculates for each station how many valid values it has available for the measurement quantity
     * that can be accessed by the mapper.
     * invalid values are registered as Double.NaN. They originate from empty or corrupt data in the source file
     *
     * @param mapper a getter method that accesses the selected quantity from a Measurement instance
     * @return A map(S, N) that provides for each station (S), the number (N) of valid measurements.
     */
    public Map<Station, Integer> numberOfValidValuesByStation(Function<Measurement, Double> mapper) {
        // Builds a map resolving for each station the number of valid values for the specified quantity.
        return this.getStations()
                .stream()
                .collect(
                        Collectors.toMap(
                                Function.identity(),
                                station -> station.numValidValues(mapper)
                        )
                );
    }

    /**
     * Calculates for each calendar year in the dataset the average daily temperatures
     * across all days in the year and all stations in this tracker
     * (invalid values shall be excluded from the averaging)
     *
     * @return A map(Y,T) that provides for each year Y the average temperature T of that year.
     */
    public Map<Integer, Double> annualAverageTemperatureTrend() {
        // Creates a submap, without invalid values, with measurements mapped per year.
        var validMeasurements =
                this.getValidMeasurementsGroupedBy(Measurement::getAverageTemperature, m -> m.getDate().getYear());

        return validMeasurements
                // Calculates the average of the measurements per year.
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                measurements -> measurements.getValue()
                                        .stream()
                                        .mapToDouble(Measurement::getAverageTemperature)
                                        .average().orElse(Double.NaN)
                        )
                );
    }

    /**
     * Calculates for each calendar year in the dataset the maximum of the selected daily quantity
     * across all days in the year and all stations in this tracker
     * (invalid values shall be excluded from the maximum aggregation)
     * (this method can be reused for maximum aggregation of different quantities in the Measurement data
     *
     * @param mapper A getter function on the Measurement class
     *               that selects the appropriate value for the maximum aggregation procedure
     * @return A map(Y,Q) that provides for each year Y the maximum value Q of the specified quantity
     */
    public Map<Integer, Double> annualMaximumTrend(Function<Measurement, Double> mapper) {
        // Creates a submap, without invalid values, with measurements mapped per year.
        var validMeasurements =
                this.getValidMeasurementsGroupedBy(mapper, m -> m.getDate().getYear());

        return validMeasurements
                // Creates a map, with each key of the submap, having the highest value or Double.NaN.
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                measurements -> measurements.getValue()
                                        .stream()
                                        .mapToDouble(mapper::apply)
                                        .max().orElse(Double.NaN)
                        )
                );
    }

    /**
     * Calculates for each of the 12 calendar months the average daily hours of sunshine
     * across all years and all stations
     * The graph of these numbers can be found in touristic brochures.
     * (invalid values shall be excluded from the averaging)
     *
     * @return a map(M,SQ) that provides for each month M the average daily sunshine hours SQ across all times
     */
    public Map<Month, Double> allTimeAverageDailySolarByMonth() {
        // Creates a submap, without invalid values, with measurements mapped per year.
        var validMeasurements = this.getValidMeasurementsGroupedBy(
                Measurement::getSolarHours, m -> m.getDate().getMonth());

        return validMeasurements
                // builds a map collecting for each month the average value of daily sunshine hours
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                measurements -> measurements.getValue()
                                        .stream()
                                        .mapToDouble(Measurement::getSolarHours)
                                        .average().orElse(Double.NaN),
                                (v1, v2) -> v1,
                                TreeMap::new
                        )
                );
    }

    /**
     * Calculate the coldest year of all time.
     * The coldest year is defined as the year with the lowest total sum of daily minimum temperatures below zero
     * accumulated across all stations
     * I.e. any daily value of a minimum temperature at a station that is above zero should not be included in its sum for the year
     * The lowest yearsum of negative minimum temperatures indicates the coldest year.
     *
     * @return the coldest year (a number between 1900 and 2099)
     * return -1 if no valid minimum temperature measurements are available
     */
    public int coldestYear() {
        final int START_VALID_YEAR_RANGE = 1900;
        final int END_VALID_YEAR_RANGE = 2099;

        // Creates a submap, without invalid values, with measurements mapped per year.
        var validMeasurements = this.getValidMeasurementsGroupedBy(
                Measurement::getMinTemperature, m -> m.getDate().getYear());

        return validMeasurements
                // Sums all values smaller than 0, of years between the valid range.
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey() >= START_VALID_YEAR_RANGE && entry.getKey() <= END_VALID_YEAR_RANGE)
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                measurements -> measurements.getValue()
                                        .stream()
                                        .mapToDouble(Measurement::getMinTemperature)
                                        .filter(minTemp -> minTemp < 0)
                                        .sum()
                        )
                )

                // Sorts the list based on the previously summed values.
                // Selects the year with lowest summed a.k.a. the coldest year, or -1 if none is found.
                .entrySet()
                .stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))

                .orElse(Map.entry(-1, Double.NaN))

                // Returns the key from map entry.
                .getKey();
    }

    /**
     * Groups the all valid values, assessment of validity is only done on the value provided by
     * the validityCheckSupplier, grouping is done based on the grouper function.
     *
     * @param validityCheckSupplier Supplies the value to assessed for validity.
     * @param grouper               Provides value to group a measurement by.
     * @param <T>                   Type of the key of the to be created map.
     * @return A map grouped with valid measurements, grouped by the grouper function.
     */
    private <T> Map<T, List<Measurement>> getValidMeasurementsGroupedBy(
            Function<Measurement, Double> validityCheckSupplier,
            Function<Measurement, T> grouper
    ) {
        Predicate<Measurement> isValidNumber = measurement -> !Double.isNaN(validityCheckSupplier.apply(measurement));

        return this.getStations()
                // Collects all valid measurements, groups them with the provided function.
                .stream()
                .flatMap(station -> station.getMeasurements().stream())
                .filter(isValidNumber)
                .collect(Collectors.groupingBy(grouper));
    }

    /**
     * imports all station and measurement information
     *
     * @param folderPath
     */
    public void importClimateDataFromVault(String folderPath) {
        this.stations.clear();

        // load all stations from the text file
        importItemsFromFile(this.stations,
                folderPath + "/stations.txt", null,
                Station::fromLine, Station::getStn);

        // load all measurements from the folder
        importMeasurementsFromVault(folderPath + "/measurements");
    }

    /**
     * traverses the purchases vault recursively and processes every data file that it finds
     *
     * @param filePath
     */
    public void importMeasurementsFromVault(String filePath) {

        File file = new File(filePath);

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            //  retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // merge all purchases of all files and sub folders from the filesInDirectory list, recursively.
            for (File f : filesInDirectory) {
                this.importMeasurementsFromVault(f.getAbsolutePath());
            }
        } else if (file.getName().matches(MEASUREMENTS_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw purchase files
            // merge the content of this file into this.purchases
            this.importMeasurementsFromFile(file.getAbsolutePath());
        }
    }

    /**
     * imports a collection of items from a text file which provides one line for each item
     *
     * @param items     the list to which imported items shall be added
     * @param filePath  the file path of the source text file
     * @param converter a function that can convert a text line into a new item instance
     * @param <E>       the (generic) type of each item
     */
    private static <K, E> void importItemsFromFile(
            Map<K, E> items,
            String filePath,
            String headerPrefix,
            Function<String, E> converter,
            Function<E, K> mapper
    ) {
        int originalNumItems = items.size();

        Scanner scanner = createFileScanner(filePath);

        // skip lines until the header is hit
        while (headerPrefix != null && scanner.hasNext()) {
            // import another line of the header
            String line = scanner.nextLine();

            if (line.startsWith(headerPrefix)) headerPrefix = null;
        }

        //  read all remaining source lines from the scanner,
        //  convert each line to an item of type E and
        //  and add each item to the map
        int newCount = 0;
        while (scanner.hasNext()) {
            // input another line
            String line = scanner.nextLine();

            // convert the line into an instance of E
            E newItem = converter.apply(line);

            //System.out.printf("extracted %s from line: %s\n", newItem, line);

            //  put the item into the map after successful conversion,
            //  using the mapper to determine the key
            //  do not overwrite existing items in the map
            if (newItem != null) {
                items.putIfAbsent(mapper.apply(newItem), newItem);
                newCount++;
            }
        }

        if (items.size() < originalNumItems + newCount) {
            throw new InputMismatchException(String.format("Duplicate items found in file %s", filePath));
        }

        //System.out.printf("Imported %d items from %s.\n", items.size() - originalNumItems, filePath);
    }

    /**
     * imports another batch of raw purchase data from the filePath text file
     * and merges the purchase amounts with the earlier imported and accumulated collection in this.purchases
     *
     * @param filePath
     */
    private void importMeasurementsFromFile(String filePath) {

        // create a temporary map to import the measurements, organised by date
        Map<LocalDate, Measurement> newMeasurementsByDate = new HashMap<>();

        // import all measurements from the specified file into the newMeasurementsByDate map
        importItemsFromFile(newMeasurementsByDate, filePath, "# STN",
                s -> Measurement.fromLine(s, this.stations), Measurement::getDate);

        //  add the measurements to their station
        //  (all measurements from the same file should belong to the same station)
        if (newMeasurementsByDate.size() > 0) {
            Station station = newMeasurementsByDate.values().stream().findAny().get().getStation();
            // add the newMeasurements to the map in the station
            int numAdded = station.addMeasurements(newMeasurementsByDate.values());
            if (numAdded != newMeasurementsByDate.size()) {
                throw new InputMismatchException(String.format("Some items in file %s could not be added", filePath));
            }
        }
    }

    /**
     * helper method to create a scanner on a file an handle the exception
     *
     * @param filePath
     * @return
     */
    private static Scanner createFileScanner(String filePath) {
        try {
            return new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + filePath);
        }
    }
}
