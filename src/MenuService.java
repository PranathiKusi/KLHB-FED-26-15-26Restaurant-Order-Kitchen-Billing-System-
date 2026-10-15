import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Module M02 - Menu Management.
 * The menu is a MenuItem[] array (capacity 30). Each slot holds a VegItem, NonVegItem
 * or Beverage object - the array type is the parent class (polymorphism).
 * Run this file alone:  java MenuService
 */
public class MenuService {
    public static final int CAPACITY = 30;
    public static final double MIN_PRICE = 10;
    public static final double MAX_PRICE = 2000;

    private final MenuItem[] menu = new MenuItem[CAPACITY];
    private int count;

    /** Loads the 13 starting dishes used throughout the DineSync documentation. */
    public void loadDefaultMenu() {
        Object[][] rows = {
            {"Veg Spring Roll", 1, 1, 150.0, 12}, {"Gobi Manchurian", 1, 1, 160.0, 15},
            {"Veg Biryani", 2, 1, 250.0, 20},     {"Paneer Tikka", 1, 1, 220.0, 15},
            {"Chicken Biryani", 2, 2, 320.0, 25}, {"Butter Naan", 3, 1, 50.0, 8},
            {"Chicken 65", 1, 2, 240.0, 15},      {"Dal Tadka", 2, 1, 180.0, 12},
            {"Fish Curry", 2, 2, 350.0, 25},      {"Gulab Jamun", 4, 1, 90.0, 5},
            {"Masala Chai", 5, 3, 40.0, 5},       {"Fresh Lime Soda", 5, 3, 60.0, 3},
            {"Mutton Rogan Josh", 2, 2, 420.0, 30}
        };
        for (Object[] r : rows) {
            // (Integer) / (Double) are casts from Object back to the wrapper type; unboxing gives int/double
            menu[count] = create(count + 1, (String) r[0], (Integer) r[1], (Integer) r[2], (Double) r[3],
                    (Integer) r[4], true);
            count++;
        }
    }

    /** Factory: chooses the subclass from the type number. */
    private static MenuItem create(int no, String name, int category, int type, double price, int prep,
                                   boolean available) {
        switch (type) {
            case MenuItem.TYPE_VEG:
                return new VegItem(no, name, category, price, prep, available);
            case MenuItem.TYPE_NON_VEG:
                return new NonVegItem(no, name, category, price, prep, available);
            default:
                return new Beverage(no, name, price, prep, available);
        }
    }

    /** "  paneer   tikka " -> "Paneer Tikka" using trim, split, substring and String.join. */
    public static String toTitleCase(String raw) {
        String[] words = raw.trim().split("\\s+");
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            words[i] = w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }

    public static void validateName(String name) throws InvalidDishException {
        String n = (name == null) ? "" : name.trim();
        if (n.length() < 3 || n.length() > 25) {
            throw new InvalidDishException("dish name must be 3-25 characters");
        }
        for (char c : n.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '\'' || c == '&')) {
                throw new InvalidDishException("dish name may contain letters, digits, spaces, - ' & only");
            }
        }
        if (!Character.isLetter(n.charAt(0))) {
            throw new InvalidDishException("dish name must start with a letter");
        }
    }

    public static void validatePrice(double price) throws InvalidPriceException {
        if (price < MIN_PRICE || price > MAX_PRICE) {
            throw new InvalidPriceException("price must be " + Colors.rupee() + "10-" + Colors.rupee() + "2000");
        }
    }

    /** Adds a new dish (Admin only). Checks run in the order given by the project document. */
    public MenuItem addDish(Staff actor, String name, int category, int type, double price, int prep,
                            boolean available)
            throws UnauthorizedAccessException, MenuFullException, DuplicateDishException,
                   InvalidPriceException, InvalidDishException {
        AuthService.requireRole(actor, Staff.ADMIN);
        if (count == CAPACITY) {
            throw new MenuFullException("menu already has " + CAPACITY + " dishes");
        }
        validateName(name);
        String clean = toTitleCase(name);
        if (findByName(clean).isPresent()) {
            throw new DuplicateDishException("\"" + findByName(clean).get().getName() + "\" exists");
        }
        if (category < 1 || category > 5) {
            throw new InvalidDishException("category must be 1-5");
        }
        if (type < 1 || type > 3) {
            throw new InvalidDishException("type must be 1 Veg, 2 Non-Veg or 3 Beverage");
        }
        // Rule chosen for consistency: Beverage type <-> Beverages category
        if ((type == MenuItem.TYPE_BEVERAGE) != (category == MenuItem.CAT_BEVERAGES)) {
            throw new InvalidDishException("Beverage type must use category 5 Beverages (and only beverages)");
        }
        validatePrice(price);
        if (prep < 1 || prep > 60) {
            throw new InvalidDishException("preparation time must be 1-60 minutes");
        }
        MenuItem item = create(count + 1, clean, category, type, price, prep, available);
        menu[count++] = item;
        return item;
    }

    public void reprice(Staff actor, int itemNo, double newPrice)
            throws UnauthorizedAccessException, InvalidPriceException, ItemUnavailableException {
        AuthService.requireRole(actor, Staff.ADMIN);
        validatePrice(newPrice);
        getByNumber(itemNo).setPrice(newPrice);
    }

    public void setAvailability(Staff actor, int itemNo, boolean available)
            throws UnauthorizedAccessException, ItemUnavailableException {
        AuthService.requireRole(actor, Staff.ADMIN);
        getByNumber(itemNo).setAvailable(available);
    }

    /** Item numbers start at 1, so item n is stored at index n - 1. */
    public MenuItem getByNumber(int itemNo) throws ItemUnavailableException {
        if (itemNo < 1 || itemNo > count) {
            throw new ItemUnavailableException("dish no. " + itemNo + " does not exist (1-" + count + ")");
        }
        return menu[itemNo - 1];
    }

    /** Case-insensitive exact name lookup; Optional means "maybe a value, maybe empty". */
    public Optional<MenuItem> findByName(String name) {
        String target = name.trim();
        return Arrays.stream(menu, 0, count)
                .filter(m -> m.getName().equalsIgnoreCase(target))
                .findFirst();
    }

    /** Generic filter: the caller passes the rule as a Predicate (lambda). */
    public List<MenuItem> filter(Predicate<MenuItem> rule) {
        List<MenuItem> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (rule.test(menu[i])) {
                result.add(menu[i]);
            }
        }
        return result;
    }

    /** Keyword search, e.g. "biryani" finds Veg Biryani and Chicken Biryani. */
    public List<MenuItem> search(String keyword) {
        String k = keyword.trim().toLowerCase();
        return filter(m -> m.getName().toLowerCase().contains(k));
    }

    /** Overloaded search: by category number instead of by keyword. */
    public List<MenuItem> search(int category) {
        return filter(m -> m.getCategory() == category);
    }

    /** Copy of the menu sorted by price (Comparator built with a method reference). */
    public MenuItem[] sortedByPrice() {
        MenuItem[] copy = Arrays.copyOf(menu, count);
        Arrays.sort(copy, Comparator.comparingDouble(MenuItem::getPrice));
        return copy;
    }

    /** Copy sorted A-Z using MenuItem's own compareTo (Comparable). */
    public MenuItem[] sortedByName() {
        MenuItem[] copy = Arrays.copyOf(menu, count);
        Arrays.sort(copy);
        return copy;
    }

    /**
     * Menu prepared for one customer (M03 -> M04): unavailable dishes hidden,
     * non-veg hidden for veg-only customers, favourites placed first.
     */
    public MenuItem[] menuFor(Customer c) {
        MenuItem[] result = new MenuItem[count];
        int n = 0;
        int[] favs = (c == null) ? new int[0] : c.topFavourites(3);
        for (int fav : favs) {
            MenuItem m = menu[fav - 1];
            if (allowedFor(m, c)) {
                result[n++] = m;
            }
        }
        for (int i = 0; i < count; i++) {
            MenuItem m = menu[i];
            boolean isFav = false;
            for (int fav : favs) {
                if (fav == m.getItemNo()) {
                    isFav = true;
                    break;
                }
            }
            if (isFav || !allowedFor(m, c)) {
                continue;
            }
            result[n++] = m;
        }
        return Arrays.copyOf(result, n);
    }

    private static boolean allowedFor(MenuItem m, Customer c) {
        return m.isAvailable() && (c == null || !c.isVegOnly() || m.isVeg());
    }

    public MenuItem[] getAll() { return Arrays.copyOf(menu, count); }

    public int getCount() { return count; }

    /** Standalone demo of module M02. */
    public static void main(String[] args) {
        MenuService ms = new MenuService();
        ms.loadDefaultMenu();
        Staff admin = new Staff("A401", "Suresh", Staff.ADMIN, "9999");
        System.out.println(Colors.title("== M02 Menu Management - standalone demo =="));
        String[] names = {"paneer tikka", "Mirchi Bajji", "Mirchi Bajji"};
        double[] prices = {220, 5, 120};
        for (int i = 0; i < names.length; i++) {
            try {
                MenuItem m = ms.addDish(admin, names[i], 1, 1, prices[i], 10, true);
                System.out.println(Colors.ok("Added " + m));
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        System.out.println("Search 'biryani': " + ms.search("biryani"));
        System.out.println("Cheapest dish: " + ms.sortedByPrice()[0]);
    }
}
