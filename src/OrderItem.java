
/**
 * One line of an order: a dish and its quantity.
 * The unit price is copied when the line is created, so a later re-price by the
 * Admin does not change an order that was already taken.
 */
public class OrderItem {
    private final MenuItem item;
    private final double unitPrice;
    private int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.unitPrice = item.getPrice();
        this.quantity = quantity;
    }

    public void addQuantity(int extra) { quantity += extra; }

    public double getLineTotal() { return unitPrice * quantity; }

    public MenuItem getItem() { return item; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() {
        return item.getName() + " x" + quantity;
    }
}
