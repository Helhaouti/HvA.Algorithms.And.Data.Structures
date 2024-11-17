package models;

import java.util.Locale;

/**
 * @author ADS docenten
 * @author Rida Zeamari
 */
public class Product {
    private final long barcode;
    private String title;
    private double price;

    public Product(long barcode) {
        this.barcode = barcode;
    }

    public Product(long barcode, String title, double price) {
        this(barcode);
        this.title = title;
        this.price = price;
    }

    /**
     * parses product information from a textLine with format: barcode, title, price
     *
     * @param textLine a String that contains information about a product.
     * @return a new Product instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Product fromLine(String textLine) {
        final int BARCODE_LOCATION = 0;
        final int TITLE_LOCATION = 1;
        final int PRICE_LOCATION = 2;

        try {
            // Parses the provided string, if no string is provided throws an exception.
            String[] parsedLine = textLine.split(",");

            // Casts the values of the string to their respective types, if they are corrupt or not present
            // it throws an exception. Otherwise, a Product instance will be created and returned with given parameters.
            return new Product(
                    Long.parseLong(parsedLine[BARCODE_LOCATION].trim()),
                    parsedLine[TITLE_LOCATION].trim(),
                    Double.parseDouble(parsedLine[PRICE_LOCATION].trim())
            );
        } catch (Exception e) {
             /*  Returns null, if an exception occurs.
                This happens when the text line is incomplete or corrupt. Which causes exceptions such as:
                    Null pointer;
                    index out of bounds;
                    cast exceptions. */
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Product)) return false;
        return this.getBarcode() == ((Product) other).getBarcode();
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%d/%s/%.2f",
                this.barcode,
                this.title,
                this.price);
    }

    public long getBarcode() {
        return barcode;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

}