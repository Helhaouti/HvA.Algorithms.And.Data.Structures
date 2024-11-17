package nl.hva.ict.ads;

import java.util.Arrays;
import java.util.Locale;

/**
 * Model for the creation of Archer objects. This model allows for storage of information,
 * relating to: the name of the archer and his performance.
 *
 * The objects created from this Model, are also automatically equipped with a unique identifier,
 * the identifier starts at 135788, and incrementally increases.
 *
 * @author ADS Docenten
 * @author Hamza el Haouti
 */
public class Archer {
    public static int MAX_ARROWS = 3;
    public static int MAX_ROUNDS = 10;
    public static int FIRST_ARCHER_ID = 135788;

    private static int idTracker;

    /**
     * Constant identifier of Archer, once assigned a value is not allowed to change.
     */
    private final int id;
    private final String firstName;
    private final String lastName;
    private final int[][] scoresPerRound = new int[MAX_ROUNDS][MAX_ARROWS];

    /**
     * Constructs a new instance of Archer and assigns a unique id to the instance.
     * Each new instance should be assigned a number that is 1 higher than the last one assigned.
     * The first instance created should have ID 135788;
     *
     * @param firstName the archers first name.
     * @param lastName  the archers surname.
     */
    public Archer(String firstName, String lastName) {
        this.id = getNewId();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Retrieves a unique identifier with a value 1 increment higher than the last one used, starting with the
     * value of FIRST_ARCHER_ID.
     *
     * @return a unique identifier
     */
    private static int getNewId() {
        if (idTracker == 0) idTracker = FIRST_ARCHER_ID;
        int idToReturn = idTracker;
        idTracker++;

        return idToReturn;
    }

    /**
     * Registers the points for each of the three arrows that have been shot during a round.
     *
     * @param round  the round for which to register the points. First round has number 1.
     * @param points the points shot during the round, one for each arrow.
     */
    public void registerScoreForRound(int round, int[] points) {
        this.scoresPerRound[round - 1] = Arrays.copyOf(points, points.length);
    }

    /**
     * compares the scores/id of this archer with the scores/id of the other archer according to
     * the scoring scheme: highest total points -> least misses -> earliest registration
     * The archer with the lowest id has registered first
     *
     * @param other the other archer to compare against
     * @return negative number, zero or positive number according to Comparator convention
     */
    public int compareByHighestTotalScoreWithLeastMissesAndLowestId(Archer other) {
        final int scoreComparison = Integer.compare(other.getTotalScore(), getTotalScore());
        final int missComparison = Integer.compare(getTotalMisses(), other.getTotalMisses());
        final int registrationComparison = Integer.compare(getId(), other.getId());

        if (scoreComparison != 0) return scoreComparison;
        else if (missComparison != 0) return missComparison;
        else return registrationComparison;
    }

    /**
     * Calculates/retrieves the total score of all arrows across all rounds
     *
     * @return the total score of all arrows across all rounds
     */
    public int getTotalScore() {
        int sum = 0;

        for (int[] scores : scoresPerRound)
            for (int score : scores)
                sum += score;

        return sum;
    }

    /**
     * Calculates/retrieves the total misses of all arrows across all rounds. It does this by assessing
     * arrows are equal to zero.
     *
     * @return the total misses of all arrows across all rounds.
     */
    private int getTotalMisses() {
        int misses = 0;

        for (int[] scores : scoresPerRound)
            for (int score : scores) if (score == 0) misses++;

        return misses;
    }

    /**
     * Generates a string representation of this Archer object, with the format: "<id> (<total score>) <name>"
     *
     * @return a string representation of this Archer object
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d (%d) %s %s",
                getId(),
                getTotalScore(),
                getFirstName(),
                getLastName()
        );
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}