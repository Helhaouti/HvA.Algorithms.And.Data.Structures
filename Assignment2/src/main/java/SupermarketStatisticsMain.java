import models.Purchase;
import models.PurchaseTracker;

import java.util.Comparator;

/**
 * Reads all purchases and products from a file, and stores them in a OrderedArrayList. Afterwards this data is queried
 * to display 5 purchases with the worst sales volume and 5 with the best sales revenue.
 *
 * @author ADS docenten
 * @author Hamza el Haouti
 * @author Rida Zeâmari
 */
public class SupermarketStatisticsMain {

    public static void main(String[] args) {
        System.out.println("Welcome to the HvA Supermarket Statistics processor\n");

        var purchaseTracker = new PurchaseTracker();

        // Import data from files.
        purchaseTracker.importProductsFromVault("/products.txt");
        purchaseTracker.importPurchasesFromVault("/purchases");

        // Prints data to console, sorted by volume & revenue.
        purchaseTracker.showTops(5, "worst sales volume",
                Comparator.comparingInt(Purchase::getCount));
        purchaseTracker.showTops(5, "best sales revenue",
                Comparator.comparingDouble(Purchase::getRevenue).reversed());

        // Prints aggregated volume & revenue to console of all purchases.
        System.out.printf("Total volume of all purchases: %.0f\n", purchaseTracker.calculateTotalVolume());
        System.out.printf("Total revenue from all purchases: %.2f\n", purchaseTracker.calculateTotalRevenue());
    }

}