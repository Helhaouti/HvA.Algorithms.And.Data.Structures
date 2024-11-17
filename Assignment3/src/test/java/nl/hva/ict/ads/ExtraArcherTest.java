package nl.hva.ict.ads;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


/**
 * Tests whether the comparator of Archer works.
 */
public class ExtraArcherTest extends ArcherTest {

    @Test
    void archerIsEqualToSelf() {
        assertEquals(scoringScheme.compare(archer1, archer1), 0);
    }

    @Test
    void archerIsNotEqualToOthers() {
        assertNotEquals(scoringScheme.compare(archer1, archer2), 0);
        assertNotEquals(scoringScheme.compare(archer2, archer3), 0);
    }

    @Test
    void winnerBasedOnPointsAlone() {
        for (int round = 1; round <= Archer.MAX_ROUNDS; round++) {
            archer1.registerScoreForRound(round, scores1);
            archer2.registerScoreForRound(round, scores2);
            archer3.registerScoreForRound(round, scores3);
        }

        assertNotEquals(scoringScheme.compare(archer1, archer2), 0);
        assertNotEquals(scoringScheme.compare(archer1, archer3), 0);
    }

    @Test
    void archerWhoRegisteredFirstWins() {
        for (int round = 1; round <= Archer.MAX_ROUNDS; round++) {
            archer1.registerScoreForRound(round, scores1);
            archer2.registerScoreForRound(round, scores1);
            archer3.registerScoreForRound(round, scores1);
        }

        assertNotEquals(scoringScheme.compare(archer1, archer2), 0);
        assertNotEquals(scoringScheme.compare(archer1, archer3), 0);
    }

}