package models;

import java.util.List;
import java.util.Locale;

/**
 * @author ADS docenten
 * @author Rida Zeamari
 * @author Hamza el Haouti
 */
public class Purchase {
    private Product product;
    private int count;

    public Purchase(Product product, int count) {
        this.product = product;
        this.count = count;
    }

    /**
     * parses purchase summary information from a textLine with format: barcode, amount
     *
     * @param textLine a string that contains information about a purchase
     * @param products a list of products ordered and searchable by barcode
     *                 (i.e. the comparator of the ordered list shall consider only the barcode when comparing products)
     * @return a new Purchase instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Purchase fromLine(String textLine, List<Product> products) {
        final int BARCODE_LOCATION = 0;
        final int AMOUNT_LOCATION = 1;

        try {
            // Parses the provided string, if no string is provided throws an exception.
            String[] parsedLine = textLine.split(",");

            // Casts the values of the string to their respective types,
            // if they are corrupt or not present it throws an exception.
            long barcode = Long.parseLong(parsedLine[BARCODE_LOCATION].trim());
            int amount = Integer.parseInt(parsedLine[AMOUNT_LOCATION].trim());

            // Retrieves an equal to the provided product from the given list,
            // if the barcode is uncorrupted and present in the list, otherwise it trows an exception.
            var productFromList = products.get(products.indexOf(new Product(barcode)));

            return new Purchase(productFromList, amount);
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
    public String toString() {
        return String.format(Locale.ROOT, "%d/%s/%d/%.2f",
                this.getBarcode(),
                this.getTitle(),
                this.getCount(),
                this.getRevenue());
    }

    /**
     * Adds a delta amount to the count of the purchase summary instance.
     *
     * @param delta amount to be added to the count of the purchase summary instance.
     */
    public void addCount(int delta) {
        this.setCount(this.getCount() + delta);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Product getProduct() {
        return product;
    }

    public double getRevenue() {
        return this.getCount() * this.getPrice();
    }

    public long getBarcode() {
        return this.product.getBarcode();
    }

    public String getTitle() {
        return this.product.getTitle();
    }

    public double getPrice() {
        return this.product.getPrice();
    }
}