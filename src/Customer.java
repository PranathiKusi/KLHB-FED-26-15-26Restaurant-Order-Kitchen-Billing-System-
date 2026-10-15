
/**
 * A customer profile (module M03), identified by mobile number.
 * dishCount[itemNo] counts how many of each dish this customer has ordered.
 */
public class Customer {
    public static final int LOYALTY_EVERY = 7;
    public static final int DISH_SLOTS = 31;     // menu item numbers 1..30 (index 0 unused)

    private final String phone;     // String, not int/long: it is an identifier, never used in arithmetic
    private final String name;
    private final boolean vegOnly;
    private int visits;
    private boolean loyaltyVisit;
    private final int[] dishCount = new int[DISH_SLOTS];

    // Overloaded constructors: diet preference defaults to "Any"
    public Customer(String phone, String name) {
        this(phone, name, false);
    }

    public Customer(String phone, String name, boolean vegOnly) {
        this.phone = phone;
        this.name = name;
        this.vegOnly = vegOnly;
    }

    /** Starts a new visit. Loyalty applies when visits % 7 == 0 (7th, 14th, 21st ... visit). */
    public int checkIn() {
        visits++;
        loyaltyVisit = (visits % LOYALTY_EVERY == 0);
        return visits;
    }

    public void addDish(int itemNo, int qty) {
        if (itemNo > 0 && itemNo < DISH_SLOTS) {
            dishCount[itemNo] += qty;
        }
    }

    /**
     * Returns the item numbers of the top n dishes (highest count first).
     * Uses a selection-style search: each round picks the largest count not picked yet.
     */
    public int[] topFavourites(int n) {
        int[] result = new int[n];
        boolean[] used = new boolean[DISH_SLOTS];
        int found = 0;
        for (int round = 0; round < n; round++) {
            int best = 0;
            for (int i = 1; i < DISH_SLOTS; i++) {
                if (!used[i] && dishCount[i] > 0 && (best == 0 || dishCount[i] > dishCount[best])) {
                    best = i;
                }
            }
            if (best == 0) {
                break;                       // no more dishes with a count > 0
            }
            used[best] = true;
            result[found++] = best;
        }
        int[] trimmed = new int[found];
        System.arraycopy(result, 0, trimmed, 0, found);
        return trimmed;
    }

    public String getPhone() { return phone; }
    public String getName() { return name; }
    public boolean isVegOnly() { return vegOnly; }
    public int getVisits() { return visits; }
    public boolean isLoyaltyVisit() { return loyaltyVisit; }
    public int getDishCount(int itemNo) {
        return (itemNo > 0 && itemNo < DISH_SLOTS) ? dishCount[itemNo] : 0;
    }

    /** Used only by demo fixtures to load a returning customer's history. */
    public void loadHistory(int pastVisits) { this.visits = pastVisits; }

    @Override
    public String toString() {
        return name + " (" + phone + ", visit #" + visits + ", " + (vegOnly ? "Veg only" : "Any diet") + ")";
    }
}
