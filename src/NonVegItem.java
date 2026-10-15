
/** A non-vegetarian dish. Hidden from veg-only customers in M04. */
public class NonVegItem extends MenuItem {

    public NonVegItem(int itemNo, String name, int category, double price, int prepMinutes, boolean available) {
        super(itemNo, name, category, price, prepMinutes, available);
    }

    @Override
    public boolean isVeg() { return false; }

    @Override
    public String getTypeLabel() { return "Non-Veg"; }

    @Override
    public String kitchenStation() { return "Non-veg station"; }

    @Override
    public int getTypeCode() { return TYPE_NON_VEG; }
}
