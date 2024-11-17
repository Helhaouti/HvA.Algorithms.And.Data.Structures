package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

/**
 * @author ADS docenten
 * @author Rida Zeamari
 */
public class PurchaseTracker {

    /**
     * The reference list of all Products available from the SuperMarket chain.
     */
    private final OrderedList<Product> products;

    /**
     * The aggregated volumes of all purchases of all products across all branches.
     */
    private final OrderedList<Purchase> purchases;

    public PurchaseTracker() {
        products = new OrderedArrayList<>(Comparator.comparing(Product::getBarcode));
        purchases = new OrderedArrayList<>(Comparator.comparing(Purchase::getBarcode));
    }

    /**
     * Imports all products from a resource file that is common to all branches of the Supermarket chain.
     *
     * @param resourceName name of the file.
     */
    public void importProductsFromVault(String resourceName) {
        this.products.clear();

        // loads all products from the text file.
        importItemsFromFile(this.products,
                Objects.requireNonNull(PurchaseTracker.class.getResource(resourceName)).getPath(),
                Product::fromLine);

        // sorts the products for efficient later retrieval.
        this.products.sort();

        System.out.printf("Imported %d products from %s.\n",
                products.size(),
                resourceName);
    }

    /**
     * Imports and merges all raw purchase data of all branches from the hierarchical file structure of the vault.
     *
     * @param resourceName name of the file.
     */
    public void importPurchasesFromVault(String resourceName) {
        this.purchases.clear();

        mergePurchasesFromVaultRecursively(
                Objects.requireNonNull(PurchaseTracker.class.getResource(resourceName)).getPath());

        System.out.printf("Accumulated purchases of %d products from files in %s.\n",
                this.purchases.size(),
                resourceName);
    }

    /**
     * Traverses the purchases vault recursively and processes every data file that it finds.
     *
     * @param filePath the file path of the source text file.
     */
    private void mergePurchasesFromVaultRecursively(String filePath) {
        File file = new File(filePath);

        String PURCHASE_FILE_PATTERN = ".*\\.txt";
        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory).
            // retrieve a list of all files and sub folders in this directory.
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);

            // merges all purchases of all files and sub folders from the filesInDirectory list, recursively.
            for (File fileEntry : filesInDirectory) mergePurchasesFromVaultRecursively(fileEntry.getAbsolutePath());
        } else if (file.getName().matches(PURCHASE_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw purchase files.
            // merge the content of this file into this.purchases.
            this.mergePurchasesFromFile(file.getAbsolutePath());
        }
    }

    /**
     * Imports another batch of raw purchase data from the filePath text file.
     * and merges the purchase amounts with the earlier imported and accumulated collection in this.purchases.
     *
     * @param filePath the file path of the source text file.
     */
    private void mergePurchasesFromFile(String filePath) {
        // create a temporary ordered list for the additional purchases, ordered by same comparator as the main list.
        OrderedList<Purchase> newPurchases = new OrderedArrayList<>(this.purchases.getOrdening());

        // re-sort the accumulated purchases for efficient searching.
        this.purchases.sort();

        // imports all purchases from the specified file into the newPurchases list.
        importItemsFromFile(newPurchases, filePath, Line -> Purchase.fromLine(Line, this.getProducts()));

        // merges all purchases from the newPurchases list into this.purchases.
        for (Purchase purchase : newPurchases) {
            this.purchases.merge(purchase, (p1, p2) -> {
                p1.addCount(p2.getCount());
                return p1;
            });
        }
    }

    /**
     * Show the top n purchases according to the ranking criteria specified by ranker.
     *
     * @param n        the number of top purchases to be shown.
     * @param subTitle some title text that clarifies the list.
     * @param ranker   the comparator used to rank the purchases.
     */
    public void showTops(int n, String subTitle, Comparator<Purchase> ranker) {
        System.out.printf("%d purchases with %s:\n", n, subTitle);
        // helper list to rank the purchases without disturbing the order of the original list.
        OrderedList<Purchase> tops = new OrderedArrayList<>(ranker);

        // add all purchases to the new tops list, and sort the list.
        tops.addAll(this.purchases);
        tops.sort();

        // show the top items
        for (int rank = 0; rank < n && rank < tops.size(); rank++) {
            System.out.printf("%d: %s\n", rank + 1, tops.get(rank));
        }
    }

    /**
     * Calculates the total volume of all purchases.
     *
     * @return the total amount of product items purchased across all purchases.
     */
    public double calculateTotalVolume() {
        return this.purchases.aggregate(Purchase::getCount);
    }

    /**
     * Calculates the total revenue of all purchases.
     *
     * @return the total amount of money paid across all purchases.
     */
    public double calculateTotalRevenue() {
        return this.purchases.aggregate(Purchase::getRevenue);
    }

    /**
     * Imports a collection of items from a text file which provides one line for each item.
     *
     * @param items     the list to which imported items shall be added.
     * @param filePath  the file path of the source text file.
     * @param converter a function that can convert a text line into a new item instance.
     * @param <E>       the (generic) type of each item.
     */
    public static <E> void importItemsFromFile(List<E> items, String filePath, Function<String, E> converter) {
        Scanner scanner = createFileScanner(filePath);

        while (scanner.hasNext()) {
            // input another line with author information.
            String line = scanner.nextLine();

            // converts the line to an instance of E.
            E item = converter.apply(line);

            // adds the item to the list of items.
            items.add(item);
        }
    }

    /**
     * Helper method to create a scanner on a file and handle the exception.
     *
     * @param filePath the file path of the source text file.
     * @return scanner.
     */
    private static Scanner createFileScanner(String filePath) {
        try {
            return new Scanner(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + filePath);
        }
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Purchase> getPurchases() {
        return purchases;
    }
}
