
/** A drink. Treated as vegetarian, prepared at the beverage counter. */
public class Beverage extends MenuItem {

    public Beverage(int itemNo, String name, double price, int prepMinutes, boolean available) {
        // Beverages always belong to category 5 (Beverages)
        super(itemNo, name, CAT_BEVERAGES, price, prepMinutes, available);
    }

    @Override
    public boolean isVeg() { return true; }

    @Override
    public String getTypeLabel() { return "Beverage"; }

    @Override
    public String kitchenStation() { return "Beverage counter"; }

    @Override
    public int getTypeCode() { return TYPE_BEVERAGE; }
}
