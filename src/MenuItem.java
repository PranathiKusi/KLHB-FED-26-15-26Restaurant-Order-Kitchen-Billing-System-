

/**
 * Abstract parent of every dish (module M02).
 * "abstract" means we never create a plain MenuItem; we create a VegItem, NonVegItem or Beverage.
 * Each subclass IS-A MenuItem (hierarchical inheritance) and overrides the abstract methods.
 * Implements Comparable so Arrays.sort() can order dishes alphabetically by name.
 */
public abstract class MenuItem implements Comparable<MenuItem> {
    public static final int TYPE_VEG = 1;
    public static final int TYPE_NON_VEG = 2;
    public static final int TYPE_BEVERAGE = 3;
    public static final int CAT_BEVERAGES = 5;
    private static final String[] CATEGORY_NAMES =
            {"", "Starters", "Main Course", "Breads", "Desserts", "Beverages"};

    private final int itemNo;
    private final String name;
    private final int category;
    private double price;
    private final int prepMinutes;
    private boolean available;

    // Parameterized constructor
    protected MenuItem(int itemNo, String name, int category, double price, int prepMinutes, boolean available) {
        this.itemNo = itemNo;
        this.name = name;
        this.category = category;
        this.price = price;
        this.prepMinutes = prepMinutes;
        this.available = available;
    }

    // Overloaded constructor: availability defaults to true. this(...) calls the other constructor.
    protected MenuItem(int itemNo, String name, int category, double price, int prepMinutes) {
        this(itemNo, name, category, price, prepMinutes, true);
    }

    // ---- abstract methods: every subclass must override these ----
    public abstract boolean isVeg();

    public abstract String getTypeLabel();

    /** Which kitchen station prepares this item (shown on kitchen tickets). */
    public abstract String kitchenStation();

    public abstract int getTypeCode();

    public static String categoryName(int category) {
        return (category >= 1 && category <= 5) ? CATEGORY_NAMES[category] : "Unknown";
    }

    public int getItemNo() { return itemNo; }
    public String getName() { return name; }
    public int getCategory() { return category; }
    public String getCategoryName() { return categoryName(category); }
    public double getPrice() { return price; }
    public int getPrepMinutes() { return prepMinutes; }
    public boolean isAvailable() { return available; }

    public void setPrice(double price) { this.price = price; }       // range checked by MenuService
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public int compareTo(MenuItem other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        String dot = " " + Colors.dot() + " ";
        return "#" + itemNo + " " + name + dot + getCategoryName() + dot + getTypeLabel()
                + dot + Colors.money(price) + (available ? "" : " (unavailable)");
    }
}
