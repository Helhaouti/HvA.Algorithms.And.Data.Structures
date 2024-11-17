package models;

public abstract class Wagon {
    /**
     * A unique identifier of a Wagon.
     */
    protected final int ID;

    /**
     * Another wagon that is appended at the tail of this wagon
     * a.k.a. the successor of this wagon in a sequence
     * set to null if no successor is connected.
     */
    private Wagon nextWagon;

    /**
     * Another wagon that is prepended at the front of this wagon
     * a.k.a. the predecessor of this wagon in a sequence
     * set to null if no predecessor is connected.
     */
    private Wagon previousWagon;

    public Wagon(int wagonId) {
        this.ID = wagonId;
    }

    /**
     * Attaches the tail wagon and its connected successors behind this wagon,
     * if and only if this wagon has no wagon attached at its tail
     * and if the tail wagon has no wagon attached in front of it.
     *
     * @param tail the wagon to attach behind this wagon.
     * @throws IllegalStateException if this wagon already has a wagon appended to it.
     * @throws IllegalStateException if tail is already attached to a wagon in front of it.
     *                               The exception should include a message that reports the conflicting connection,
     *                               e.g.: "%s is already pulling %s"
     *                               or:   "%s has already been attached to %s"
     */
    public void attachTail(Wagon tail) {
        if (tail == null) return;

        if (this.hasNextWagon()) throw new IllegalStateException(
                String.format("\n%s has already been attached to %s", this, this.getNextWagon()));
        if (tail.hasPreviousWagon()) throw new IllegalStateException(
                String.format("\n%s is already pulling %s", tail.previousWagon, tail));

        tail.reAttachTo(this);
    }

    /**
     * Detaches the tail from this wagon and returns the first wagon of this tail.
     *
     * @return the first wagon of the tail that has been detached
     * or <code>null</code> if it had no wagons attached to its tail.
     */
    public Wagon detachTail() {
        if (!this.hasNextWagon()) return null;

        // Store the tail wagon, so it can be returned later on.
        final Wagon TAIL = this.getNextWagon();

        // Detach this wagon from its tail wagon.
        this.setNextWagon(null);
        TAIL.setPreviousWagon(null);

        return TAIL;
    }

    /**
     * Detaches this wagon from the wagon in front of it.
     * No action if this wagon has no previous wagon attached.
     *
     * @return the former previousWagon that this has been detached from,
     * or <code>null</code> if it had no previousWagon.
     */
    public Wagon detachFront() {
        if (!this.hasPreviousWagon()) return null;

        // Store the front wagon, so it can be returned later on.
        final Wagon FRONT = this.getPreviousWagon();

        // Detach this wagon from its front wagon.
        this.setPreviousWagon(null);
        FRONT.setNextWagon(null);

        return FRONT;
    }

    /**
     * Replaces the tail of the <code>front</code> wagon by this wagon and its connected successors
     * Before such reconfiguration can be made,
     * the method first disconnects this wagon form its predecessor,
     * and the <code>front</code> wagon from its current tail.
     *
     * @param front the wagon to which this wagon must be attached to.
     */
    public void reAttachTo(Wagon front) {
        this.detachFront();
        front.detachTail();

        front.setNextWagon(this);
        this.setPreviousWagon(front);
    }

    /**
     * Removes this wagon from the sequence that it is part of,
     * and reconnects its tail to the wagon in front of it, if any.
     */
    public void removeFromSequence() {
        // Check whether this wagon is part of any sequence.
        if (!this.hasNextWagon() && !this.hasPreviousWagon()) return;

        // Detach from the sequence and store this wagon's former tail and head.
        final Wagon TAIL = this.detachTail();
        final Wagon HEAD = this.detachFront();

        // Connect the former tail sequence to former head sequence, if any.
        if (HEAD != null) HEAD.attachTail(TAIL);
    }

    /**
     * Reverses the order in the sequence of wagons from this Wagon until its final successor.
     * The reversed sequence is attached again to the wagon in front of this Wagon, if any.
     * No action if this Wagon has no succeeding next wagon attached.
     *
     * @return the new start Wagon of the reversed sequence (with is the former last Wagon of the original sequence)
     */
    public Wagon reverseSequence() {
        Wagon previousWagon = this.getPreviousWagon();
        Wagon reverse = reverseSequence(this);

        if (previousWagon != null) {
            reverse.setPreviousWagon(previousWagon);
            previousWagon.setNextWagon(reverse);
        }

        return reverse;
    }

    private Wagon reverseSequence(Wagon wagon) {
        if (!wagon.hasNextWagon()) {
            wagon.setPreviousWagon(null);
            return wagon;
        }

        Wagon reverse = reverseSequence(wagon.getNextWagon());

        wagon.getNextWagon().setNextWagon(wagon);
        wagon.setPreviousWagon(wagon.getNextWagon());
        wagon.setNextWagon(null);

        return reverse;
    }

    /**
     * Returns the last wagon attached to it,
     * if there are no wagons attached to it then this wagon is the last wagon.
     *
     * @return the last wagon
     */
    public Wagon getLastWagonAttached() {
        Wagon wagon = this;

        while (wagon.hasNextWagon()) wagon = wagon.getNextWagon();

        return wagon;
    }

    /**
     * @return the length of the tail of wagons towards the end of the sequence
     * excluding this wagon itself.
     */
    public int getTailLength() {
        Wagon wagon = this;
        int tailLength = 0;

        while (wagon.hasNextWagon()) {
            wagon = wagon.getNextWagon();
            tailLength++;
        }

        return tailLength;
    }

    /**
     * Gets the ID.
     *
     * @return the ID.
     */
    public int getID() {
        return ID;
    }

    /**
     * Gets the next wagon of this wagon.
     *
     * @return the next wagon of this wagon.
     */
    public Wagon getNextWagon() {
        return nextWagon;
    }

    /**
     * Sets the next wagon of this wagon.
     */
    public void setNextWagon(Wagon nextWagon) {
        this.nextWagon = nextWagon;
    }

    /**
     * Gets the previous wagon of this wagon.
     *
     * @return the previous wagon of this wagon.
     */
    public Wagon getPreviousWagon() {
        return previousWagon;
    }

    /**
     * Sets the previous wagon of this wagon.
     */
    public void setPreviousWagon(Wagon previousWagon) {
        this.previousWagon = previousWagon;
    }

    /**
     * @return whether this wagon has a wagon appended at the tail
     */
    public boolean hasNextWagon() {
        return this.getNextWagon() != null;
    }

    /**
     * @return whether this wagon has a wagon prepended at the front
     */
    public boolean hasPreviousWagon() {
        return this.getPreviousWagon() != null;
    }

    /**
     * Creates a string containing the sequence of wagons attached to the tail of this wagon.
     *
     * @return A string containing the sequence of wagons attached to the tail of this wagon.
     */
    public String getTailWagonsString() {
        final StringBuilder WAGONS_NEXT = new StringBuilder();
        Wagon wagon = this.getNextWagon();

        while (wagon != null) {
            WAGONS_NEXT.append(String.format("%s", wagon));
            wagon = wagon.getNextWagon();
        }

        return WAGONS_NEXT.toString();
    }

    @Override
    public String toString() {
        return String.format("[Wagon-%s]", this.getID());
    }
}