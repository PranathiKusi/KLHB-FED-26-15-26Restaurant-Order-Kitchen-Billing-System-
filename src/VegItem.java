
/** A vegetarian dish. Single inheritance: VegItem extends MenuItem. */
public class VegItem extends MenuItem {

    public VegItem(int itemNo, String name, int category, double price, int prepMinutes, boolean available) {
        super(itemNo, name, category, price, prepMinutes, available);   // calls the parent constructor
    }

    @Override
    public boolean isVeg() { return true; }

    @Override
    public String getTypeLabel() { return "Veg"; }

    @Override
    public String kitchenStation() { return "Veg station"; }

    @Override
    public int getTypeCode() { return TYPE_VEG; }
}
