package models;

public class FreightWagon extends Wagon {
    private final int MAX_WEIGHT;

    public FreightWagon(int wagonId, int maxWeight) {
        super(wagonId);
        this.MAX_WEIGHT = maxWeight;
    }

    public int getMaxWeight() {
        return MAX_WEIGHT;
    }
}