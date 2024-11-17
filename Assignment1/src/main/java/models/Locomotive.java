package models;

public class Locomotive {
    private final int LOC_NUMBER;
    private final int MAX_WAGONS;

    public Locomotive(int locNumber, int maxWagons) {
        this.LOC_NUMBER = locNumber;
        this.MAX_WAGONS = maxWagons;
    }

    public int getMAX_WAGONS() {
        return MAX_WAGONS;
    }

    @Override
    public String toString() {
        return String.format("[Locomotive-%s]",LOC_NUMBER);
    }
}