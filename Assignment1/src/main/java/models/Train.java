package models;

public class Train {
    private final String ORIGIN;
    private final String DESTINATION;
    private final Locomotive ENGINE;
    private Wagon firstWagon;

    public Train(Locomotive engine, String origin, String destination) {
        this.ENGINE = engine;
        this.DESTINATION = destination;
        this.ORIGIN = origin;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        final Wagon WAGON_TO_BE_MOVED = this.findWagonById(wagonId);

        if (WAGON_TO_BE_MOVED == null || !toTrain.canAttach(WAGON_TO_BE_MOVED)) return false;

        final Wagon TAIL = WAGON_TO_BE_MOVED.getNextWagon();
        final boolean HEAD_ATTACHED = WAGON_TO_BE_MOVED.hasPreviousWagon();

        WAGON_TO_BE_MOVED.removeFromSequence();

        if (!HEAD_ATTACHED) this.setFirstWagon(TAIL);

        toTrain.attachToRear(WAGON_TO_BE_MOVED);
        return true;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 1 <= position <= numWagons + 1
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        final int ATTACH_TO_REAR_POSITION = this.getNumberOfWagons() + 1;
        final int FIRST_WAGON_POSITION = 1;

        if (!this.canAttach(wagon)) return false;
        wagon.detachFront();

        // Attach the wagon to rear, if the position specifies so.
        if (ATTACH_TO_REAR_POSITION != FIRST_WAGON_POSITION
                && position == ATTACH_TO_REAR_POSITION) {
            wagon.reAttachTo(this.getLastWagonAttached());
            return true;
        }

        // Insert as firstWagon or place in between two wagons in the sequence.
        final Wagon WAGON_AT_POSITION = this.findWagonAtPosition(position);

        if (position != FIRST_WAGON_POSITION && this.isPositionOutOfBounds(position)) return false;

        this.insertBeforeWagon(wagon, WAGON_AT_POSITION);
        return true;
    }

    /**
     * Inserts the toBeInsertedWagon before the current <code>wagonInSequence</code>.
     * If there is no current <code>wagonInSequence</code>, the toBeInsertedWagon is set as the firstWagon.
     *
     * @param toBeInsertedWagon The wagon to be placed in before the current <code>wagonInSequence</code>.
     * @param wagonInSequence The wagon where the <code>toBeInsertedWagon</code> should be placed in front of.
     */
    private void insertBeforeWagon(Wagon toBeInsertedWagon, Wagon wagonInSequence) {
        // Set as the firstWagon of the train or the nextWagon of the wagonInSequence.
        if (wagonInSequence == null || !wagonInSequence.hasPreviousWagon()) this.setFirstWagon(toBeInsertedWagon);
        else wagonInSequence.detachFront().attachTail(toBeInsertedWagon);

        // Attach the wagonInSequence behind the sequence of the inserted wagon, if any.
        if (wagonInSequence != null)
            wagonInSequence.reAttachTo(toBeInsertedWagon.getLastWagonAttached());
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 1 <= position <= numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        final Wagon TO_BE_MOVED = this.findWagonAtPosition(position);

        if (this.isPositionOutOfBounds(position) || !toTrain.canAttach(TO_BE_MOVED)) return false;

        if (TO_BE_MOVED == this.getFirstWagon()) this.setFirstWagon(null);

        toTrain.attachToRear(TO_BE_MOVED);
        return true;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        if (wagon == null
                || this.getENGINE() == null
                || !this.hasAdditionalCapacityFor(wagon)
                || this.hasWagonById(wagon.getID())) return false;

        return !hasWagons() || this.isCompatibleWith(wagon);
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position 1 <= position <= numWagons
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        if (this.isPositionOutOfBounds(position)) return null;

        Wagon wagon = this.getFirstWagon();

        while (position > 1) {
            wagon = wagon.getNextWagon();
            position--;
        }

        return wagon;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId A unique identifier of a Wagon.
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon wagon = this.getFirstWagon();

        while (wagon != null) {
            if (wagon.getID() == wagonId) return wagon;
            wagon = wagon.getNextWagon();
        }

        return null;
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {
        final int INSERT_POSITION = this.getNumberOfWagons() + 1;
        return insertAtPosition(INSERT_POSITION, wagon);
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        final int FIRST_WAGON_POSITION = 1;
        return this.insertAtPosition(FIRST_WAGON_POSITION, wagon);
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        if (this.hasWagons()) this.setFirstWagon(this.getFirstWagon().reverseSequence());
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        if (!isPassengerTrain()) return 0;

        int numberOfSeats = 0;
        PassengerWagon wagon = (PassengerWagon) this.getFirstWagon();

        while (wagon != null) {
            numberOfSeats += wagon.getNUMBER_OF_SEATS();
            wagon = (PassengerWagon) wagon.getNextWagon();
        }

        return numberOfSeats;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        if (!this.isFreightTrain()) return 0;

        int maxWeight = 0;
        FreightWagon wagon = (FreightWagon) this.getFirstWagon();

        while (wagon != null) {
            maxWeight += wagon.getMaxWeight();
            wagon = (FreightWagon) wagon.getNextWagon();
        }

        return maxWeight;
    }

    /**
     * Determines whether a Wagon with a given id is attached to this train.
     *
     * @param id the id of the Wagon.
     * @return Whether a wagon with the given id is connected.
     */
    private boolean hasWagonById(int id) {
        return this.findWagonById(id) != null;
    }

    /**
     * Determines whether a wagon is compatible with the current train type (passenger or freight).
     *
     * @param wagon the wagon whose compatibility needs to be assessed.
     * @return Whether the wagon is compatible with the train.
     */
    private boolean isCompatibleWith(Wagon wagon) {
        return wagon.getClass().equals(this.getFirstWagon().getClass());
    }

    /**
     * Determines whether this train has enough remaining capacity to attach the given wagon sequence to the train.
     *
     * @param wagon For which capacity needs to be determined.
     * @return Whether there is capacity available.
     */
    private boolean hasAdditionalCapacityFor(Wagon wagon) {
        final int REMAINING_CAPACITY = this.getENGINE().getMAX_WAGONS() - this.getNumberOfWagons();
        final int WAGON_SEQUENCE_LENGTH = wagon.getTailLength() + 1;

        return (REMAINING_CAPACITY - WAGON_SEQUENCE_LENGTH) >= 0;
    }

    /**
     * Determines whether a position is out of bound of the wagons sequence of this train.
     *
     * @param position The position that needs to be checked.
     * @return Whether the position is valid in this wagon sequence.
     */
    private boolean isPositionOutOfBounds(int position) {
        return position > this.getNumberOfWagons() || position <= 0;
    }

    /**
     * Determines whether this train has any wagons attached to it.
     *
     * @return whether this train has any wagons attached to it.
     */
    public boolean hasWagons() {
        return this.getFirstWagon() != null;
    }

    /**
     * Determines whether this train is a passengerTrain.
     *
     * @return whether this train is a passengerTrain.
     */
    public boolean isPassengerTrain() {
        return this.getFirstWagon() instanceof PassengerWagon;
    }

    /**
     * Determines whether this train is a freightTrain.
     *
     * @return whether this train is a freightTrain.
     */
    public boolean isFreightTrain() {
        return this.getFirstWagon() instanceof FreightWagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        final int FIRST_WAGON = 1;
        return hasWagons() ? FIRST_WAGON + this.getFirstWagon().getTailLength() : 0;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        return hasWagons() ? this.getFirstWagon().getLastWagonAttached() : null;
    }

    /**
     * Returns the engine of this train.
     *
     * @return the engine of this train.
     */
    public Locomotive getENGINE() {
        return ENGINE;
    }

    /**
     * Returns the firstWagon of this train.
     *
     * @return the firstWagon of this train.
     */
    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     * (sustaining all representation invariants)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     *              (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        if (this.getENGINE() == null) return;

        // Stores the firstWagon and sets firstWagon to null,
        // so the remaining capacity of this train can be measured.
        final Wagon OLD_FIRST_WAGON = this.getFirstWagon();
        this.firstWagon = null;

        if (wagon == null) return;
        if (!wagon.hasPreviousWagon() && this.hasAdditionalCapacityFor(wagon)) this.firstWagon = wagon;
        else this.firstWagon = OLD_FIRST_WAGON;
    }

    /**
     * Returns the origin of this train.
     *
     * @return the origin of this train.
     */
    public String getORIGIN() {
        return ORIGIN;
    }

    /**
     * Returns the destination of this train.
     *
     * @return the destination of this train.
     */
    public String getDESTINATION() {
        return DESTINATION;
    }

    /**
     * Creates a description of this Train.
     *
     * @return A description of this Train.
     */
    @Override
    public String toString() {
        final String TAIL_WAGONS_OF_FIRST_WAGON =
                this.hasWagons() ? this.getFirstWagon().getTailWagonsString() : "";

        return String.format("%s%s%s with %d wagons from %s to %s",
                this.getENGINE(),
                this.hasWagons() ? this.getFirstWagon() : "",
                TAIL_WAGONS_OF_FIRST_WAGON,
                this.getNumberOfWagons(),
                this.getORIGIN(),
                this.getDESTINATION());
    }
}