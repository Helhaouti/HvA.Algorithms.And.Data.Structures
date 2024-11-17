package models;

public class PassengerWagon extends Wagon {
    private final int NUMBER_OF_SEATS;

    public PassengerWagon(int wagonId, int numberOfSeats) {
        super(wagonId);
        this.NUMBER_OF_SEATS = numberOfSeats;
    }

    public int getNUMBER_OF_SEATS() {
        return NUMBER_OF_SEATS;
    }
}