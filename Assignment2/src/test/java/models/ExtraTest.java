package models;

import org.junit.jupiter.api.*;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ExtraTest {
    PurchaseTracker purchaseTracker;

    private OrderedList<Product> products;
    private Product stroopwafels, mars, snickers;

    @BeforeEach
    private void setup() {
        stroopwafels = new Product(111111111111111L, "Stroopwafels 10st", 1.23);
        mars = new Product(222222222222222L, "Mars", 0.86);
        snickers = new Product(333333333333333L, "Snickers", 0.97);

        products = new OrderedArrayList<>(Comparator.comparing(Product::getBarcode));
        products.addAll(List.of(stroopwafels, mars, snickers));

        purchaseTracker = new PurchaseTracker();
        purchaseTracker.importProductsFromVault("/products.txt");
        purchaseTracker.importPurchasesFromVault("/purchases");
    }

    @Test
    public void calculateTotalVolume(){
        assertEquals(16730, purchaseTracker.calculateTotalVolume());

        purchaseTracker = new PurchaseTracker();
        assertEquals(0, purchaseTracker.calculateTotalVolume());
    }

    @Test
    public void calculateTotalRevenue(){
        assertEquals(38120.37999999999, purchaseTracker.calculateTotalRevenue());

        purchaseTracker = new PurchaseTracker();
        assertEquals(0, purchaseTracker.calculateTotalRevenue());
    }

    @Test
    public void sortOrdersByNewComparator() {
        products.sort(Comparator.comparing(Product::getPrice));

        assertEquals(3, products.size());
        assertEquals(mars.getBarcode(), products.get(0).getBarcode());
        assertEquals(snickers.getBarcode(), products.get(1).getBarcode());
        assertEquals(stroopwafels.getBarcode(), products.get(2).getBarcode());
    }

    @Test
    public void compareDifferentProducts() {
        assertNotEquals(mars, snickers);
    }

    @Test
    public void purchaseFromLineIntegrity() {
        Purchase purchase;

        // Invalid property: Barcode with letters
        purchase = Purchase.fromLine(String.format("jcks%dj, %d", stroopwafels.getBarcode(), 534), products);
        assertNull(purchase);

        // Incomplete string
        purchase = Purchase.fromLine(String.format("%d", stroopwafels.getBarcode()), products);
        assertNull(purchase);

        // No string provided
        purchase = Purchase.fromLine(null, products);
        assertNull(purchase);

        // Item not present in list.
        purchase = Purchase.fromLine(String.format("%d, %d", stroopwafels.getBarcode(), 512), List.of());
        assertNull(purchase);
    }

    @Test
    public void productFromLineIntegrity() {
        Product product;

        // Invalid property: Barcode with letters
        product = Product.fromLine("1111kjdsncs1111, Mars bar, 0.90");
        assertNull(product);

        // Incomplete string
        product = Product.fromLine("222222222222222, 0.85");
        assertNull(product);

        // No string provided
        product = Product.fromLine(null);
        assertNull(product);
    }

}