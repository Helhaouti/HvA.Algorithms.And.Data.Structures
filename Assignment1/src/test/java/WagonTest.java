import models.FreightWagon;
import models.PassengerWagon;
import models.Wagon;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class WagonTest {

    Wagon passengerWagon1, passengerWagon2, passengerWagon3, passengerWagon4;
    Wagon freightWagon1, freightWagon2;

    @BeforeEach
    private void setup() {
        passengerWagon1 = new PassengerWagon(8001, 36);
        passengerWagon2 = new PassengerWagon(8002, 18);
        passengerWagon3 = new PassengerWagon(8003, 48);
        passengerWagon4 = new PassengerWagon(8004, 44);
        freightWagon1 = new FreightWagon(9001, 50000);
        freightWagon2 = new FreightWagon(9002, 60000);
    }

    @AfterEach
    private void checkRepresentationInvariants() {
        checkRepresentationInvariant(passengerWagon1);
        checkRepresentationInvariant(passengerWagon2);
        checkRepresentationInvariant(passengerWagon3);
        checkRepresentationInvariant(passengerWagon4);
        checkRepresentationInvariant(freightWagon1);
        checkRepresentationInvariant(freightWagon2);
    }
    public static void checkRepresentationInvariant(Wagon wagon) {
        // TODO check the nextWagon and previousWagon representation invariants of wagon
        assertTrue(!wagon.hasNextWagon() || wagon == wagon.getNextWagon().getPreviousWagon(),
                String.format("Wagon %s should be the previous wagon of its next wagon, if any", wagon));
        assertTrue(!wagon.hasPreviousWagon() || wagon == wagon.getPreviousWagon().getNextWagon(),
                String.format("Wagon %s should be the next wagon of its previous wagon, if any", wagon));

        //assertTrue(false);
    }

    @Test
    public void T01_AWagonCannotBeInstantiated() {
        // Dig deep ;-)
        assertTrue((Wagon.class.getModifiers() & 0x00000400) != 0);
    }

    @Test
    public void T02_APassengerWagonShouldReportCorrectProperties() {
        // check subclasses
        assertFalse(passengerWagon1 instanceof FreightWagon);

        // check properties
        assertEquals(8001, passengerWagon1.getID());
        assertEquals(36, ((PassengerWagon) passengerWagon1).getNUMBER_OF_SEATS());

        // check printed information
        assertEquals("[Wagon-8001]", passengerWagon1.toString());
    }

    @Test
    public void T02_AFreightWagonShouldReportCorrectProperties() {
        // check subclasses
        assertFalse(freightWagon1 instanceof PassengerWagon);

        // check properties
        assertEquals(9001, freightWagon1.getID());
        assertEquals(50000, ((FreightWagon) freightWagon1).getMaxWeight());

        // check printed information
        assertEquals("[Wagon-9001]", freightWagon1.toString());
    }



    @Test
    public void T03_ASingleWagonIsTheLastWagonOfASequence() {
        assertEquals(passengerWagon1, passengerWagon1.getLastWagonAttached(),
                "A single wagon should be the last wagon of its own sequence");
    }

    @Test
    public void T03_AllWagonsInASequenceShouldReturnTheLastWagonOfTheSequence() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        assertSame(passengerWagon4, passengerWagon4.getLastWagonAttached(),
                "The last attachment should become the last wagon in a sequence");
        assertSame(passengerWagon4, passengerWagon3.getLastWagonAttached(),
                "The last attachment should become the last wagon in a sequence of 2");
        assertSame(passengerWagon4, passengerWagon2.getLastWagonAttached(),
                "The last attachment should become the last wagon in a sequence of 3");
        assertSame(passengerWagon4, passengerWagon1.getLastWagonAttached(),
                "The last attachment should become the last wagon in a sequence of 4");
    }

    @Test
    public void T03E_ASingleWagonShouldHaveATailLengthOfOne() {
        passengerWagon1.setNextWagon(passengerWagon2);
        passengerWagon2.setPreviousWagon(passengerWagon1);

        assertEquals(1, passengerWagon1.getTailLength(),
                "A single wagon should have a tail of length=0");
    }

    @Test
    public void T04_ASingleWagonShouldHaveATailLengthOfZero() {
        assertEquals(0, passengerWagon1.getTailLength(),
                "A single wagon should have a tail of length=0");
    }

    @Test
    public void T04_AllWagonsInASequenceShouldReportACorrectTailLength() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        assertEquals(0, passengerWagon4.getTailLength(),
                "The last attachment in a sequence should report a tailLength=0");
        assertEquals(1, passengerWagon3.getTailLength(),
                "A wagon with one attachment should report a tailLength=1");
        assertEquals(2, passengerWagon2.getTailLength(),
                "A wagon with two attachments should report a tailLength=2");
        assertEquals(3, passengerWagon1.getTailLength(),
                "A wagon with three attachments should report a tailLength=3");
    }


    @Test
    public void T05_AttachTailCanOnlyConnectHeadWagons() {
        passengerWagon1.attachTail(passengerWagon2);

        Throwable t;

        t = assertThrows(IllegalStateException.class,
                () -> {
                    passengerWagon1.attachTail(passengerWagon2);
                }

        );
        assertTrue(t.getMessage().contains(passengerWagon1.toString().replaceAll("[\\[\\]]","")),
                "Exception message '" + t.getMessage() + "' should report that " + passengerWagon1.toString() + " is already pulling " + passengerWagon2.toString());
        assertTrue(t.getMessage().contains(passengerWagon2.toString().replaceAll("[\\[\\]]","")),
                "Exception message should report that " + passengerWagon2.toString() + " has already been attached to " + passengerWagon1.toString());

        t = assertThrows(IllegalStateException.class,
                () -> {
                    passengerWagon3.attachTail(passengerWagon2);
                }
        );
        assertTrue(t.getMessage().contains(passengerWagon1.toString().replaceAll("[\\[\\]]","")),
                "Exception message should report that " + passengerWagon2.toString() + " has already been attached to " + passengerWagon1.toString());
        assertTrue(t.getMessage().contains(passengerWagon2.toString().replaceAll("[\\[\\]]","")),
                "Exception message should report that " + passengerWagon2.toString() + " has already been attached to " + passengerWagon1.toString());

        t = assertThrows(IllegalStateException.class,
                () -> {
                    passengerWagon1.attachTail(passengerWagon3);
                }
        );
        assertTrue(t.getMessage().contains(passengerWagon1.toString().replaceAll("[\\[\\]]","")),
                "Exception message should report that " + passengerWagon1.toString() + " is already pulling " + passengerWagon2.toString());
        assertTrue(t.getMessage().contains(passengerWagon2.toString().replaceAll("[\\[\\]]","")),
                "Exception message should report that " + passengerWagon1.toString() + " is already pulling " + passengerWagon2.toString());
    }

    @Test
    public void T06_DetachTailInMiddleOfSequenceShouldResultInTwoSequences() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        Wagon oldTail = passengerWagon2.detachTail();
        assertSame(passengerWagon3, oldTail,
                "detachTail should return the formerly connected next wagon");

        assertFalse(passengerWagon1.hasPreviousWagon());
        assertSame(passengerWagon2, passengerWagon1.getNextWagon());

        assertSame(passengerWagon1, passengerWagon2.getPreviousWagon());
        assertFalse(passengerWagon2.hasNextWagon());

        assertFalse(passengerWagon3.hasPreviousWagon());
        assertSame(passengerWagon4, passengerWagon3.getNextWagon());

        assertSame(passengerWagon3, passengerWagon4.getPreviousWagon());
        assertFalse(passengerWagon4.hasNextWagon());


        // repeated detachTail should return null;
        oldTail = passengerWagon2.detachTail();
        assertNull(oldTail,
                "detachFront should return the formerly connected next wagon, if any");

        assertFalse(passengerWagon1.hasPreviousWagon());
        assertSame(passengerWagon2, passengerWagon1.getNextWagon());

        assertSame(passengerWagon1, passengerWagon2.getPreviousWagon());
        assertFalse(passengerWagon2.hasNextWagon());
    }

    @Test
    public void T07_DetachFrontInMiddleOfSequenceShouldResultInTwoSequences() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        Wagon oldFront = passengerWagon3.detachFront();

        assertSame(passengerWagon2, oldFront,
                "detachFront should return the formerly connected previous wagon");

        assertFalse(passengerWagon1.hasPreviousWagon());
        assertSame(passengerWagon2, passengerWagon1.getNextWagon());

        assertSame(passengerWagon1, passengerWagon2.getPreviousWagon());
        assertFalse(passengerWagon2.hasNextWagon());

        assertFalse(passengerWagon3.hasPreviousWagon());
        assertSame(passengerWagon4, passengerWagon3.getNextWagon());

        assertSame(passengerWagon3, passengerWagon4.getPreviousWagon());
        assertFalse(passengerWagon4.hasNextWagon());

        // repeated detach should return null without other change
        oldFront = passengerWagon3.detachFront();

        assertNull(oldFront,
                "detachFront should return the formerly connected previous wagon, if any");

        assertFalse(passengerWagon3.hasPreviousWagon());
        assertSame(passengerWagon4, passengerWagon3.getNextWagon());

        assertSame(passengerWagon3, passengerWagon4.getPreviousWagon());
        assertFalse(passengerWagon4.hasNextWagon());
    }

    @Test
    public void T08_ReAttachShouldMoveWagonToNewSequence() {
        // Two separate sequences!
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon3.attachTail(passengerWagon4);

        passengerWagon4.reAttachTo(passengerWagon2);

        assertFalse(passengerWagon3.hasNextWagon());
        assertFalse(passengerWagon3.hasPreviousWagon());

        assertSame(passengerWagon2, passengerWagon1.getNextWagon());
        assertFalse(passengerWagon1.hasPreviousWagon());

        assertSame(passengerWagon4, passengerWagon2.getNextWagon());
        assertSame(passengerWagon1, passengerWagon2.getPreviousWagon());

        assertFalse(passengerWagon4.hasNextWagon());
        assertSame(passengerWagon2, passengerWagon4.getPreviousWagon());
    }

    @Test
    public void T09_RemoveFirstWagonFromThreeShouldResultInSequenceOfTwo() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);

        // remove first wagon
        passengerWagon1.removeFromSequence();

        assertFalse(passengerWagon1.hasNextWagon());
        assertFalse(passengerWagon1.hasPreviousWagon());

        assertEquals(passengerWagon3, passengerWagon2.getNextWagon());
        assertFalse(passengerWagon2.hasPreviousWagon());

        assertFalse(passengerWagon3.hasNextWagon());
        assertEquals(passengerWagon2, passengerWagon3.getPreviousWagon());
    }

    @Test
    public void T09_RemoveMiddleWagonFromThreeShouldResultInSequenceOfTwo() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);

        // remove middle wagon
        passengerWagon2.removeFromSequence();

        assertFalse(passengerWagon2.hasNextWagon());
        assertFalse(passengerWagon2.hasPreviousWagon());

        assertEquals(passengerWagon3, passengerWagon1.getNextWagon());
        assertFalse(passengerWagon1.hasPreviousWagon());

        assertFalse(passengerWagon3.hasNextWagon());
        assertEquals(passengerWagon1, passengerWagon3.getPreviousWagon());
    }

    @Test
    public void T09_RemoveLastWagonFromThreeShouldResultInSequenceOfTwo() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);

        // remove final wagon
        passengerWagon3.removeFromSequence();

        assertFalse(passengerWagon3.hasNextWagon());
        assertFalse(passengerWagon3.hasPreviousWagon());

        assertEquals(passengerWagon2, passengerWagon1.getNextWagon());
        assertFalse(passengerWagon1.hasPreviousWagon());

        assertFalse(passengerWagon2.hasNextWagon());
        assertEquals(passengerWagon1, passengerWagon2.getPreviousWagon());
    }

    /**
     * Regarding grading, reverse is not necessary for a sufficient result.
     */
    @Test
    public void T10_WholeSequenceOfFourShouldBeReversed() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        // reverse full sequence
        Wagon rev = passengerWagon1.reverseSequence();

        assertEquals(3, rev.getTailLength());
        assertEquals(passengerWagon4, rev);
        assertEquals(passengerWagon3, rev.getNextWagon());
        assertFalse(rev.hasPreviousWagon());

        assertEquals(passengerWagon2, passengerWagon3.getNextWagon());
        assertEquals(passengerWagon4, passengerWagon3.getPreviousWagon());

        assertEquals(passengerWagon1, passengerWagon2.getNextWagon());
        assertEquals(passengerWagon3, passengerWagon2.getPreviousWagon());

        assertFalse(passengerWagon1.hasNextWagon());
        assertEquals(passengerWagon2, passengerWagon1.getPreviousWagon());
    }

    /**
     * Regarding grading, reverse is not necessary for a sufficient result.
     */
    @Test
    public void T10_PartiallyReverseASequenceOfFour() {
        passengerWagon1.attachTail(passengerWagon2);
        passengerWagon2.attachTail(passengerWagon3);
        passengerWagon3.attachTail(passengerWagon4);

        // reverse part of the sequence
        Wagon rev = passengerWagon3.reverseSequence();
        assertEquals(1, rev.getTailLength(), "After reversing the middle wagon, the tail length should remain the same");
        assertEquals(passengerWagon4, rev);

        assertEquals(passengerWagon3, rev.getNextWagon());
        assertEquals(passengerWagon2, rev.getPreviousWagon());

        assertFalse(passengerWagon3.hasNextWagon());
        assertEquals(passengerWagon4, passengerWagon3.getPreviousWagon());

        assertEquals(3, passengerWagon1.getTailLength());
        assertFalse(passengerWagon1.hasPreviousWagon());
        assertEquals(passengerWagon2, passengerWagon1.getNextWagon());

        assertEquals(passengerWagon1, passengerWagon2.getPreviousWagon());
        assertEquals(passengerWagon4, passengerWagon2.getNextWagon());
    }
}
