/**
 * Module M03 - Customer Profile and Favourites.
 * Customers live in a Customer[] array (capacity 100) and are found by mobile number (linear search).
 * Run this file alone:  java CustomerService
 */
public class CustomerService {
    public static final int CAPACITY = 100;

    private final Customer[] customers = new Customer[CAPACITY];
    private int count;

    /** 10 digits, all digits, first digit 6-9. */
    public static String validatePhone(String phone) throws InvalidPhoneNumberException {
        String p = (phone == null) ? "" : phone.trim();
        if (p.length() != 10) {
            throw new InvalidPhoneNumberException("mobile number must have 10 digits (you entered " + p.length() + ")");
        }
        for (int i = 0; i < p.length(); i++) {
            if (!Character.isDigit(p.charAt(i))) {
                throw new InvalidPhoneNumberException("digits only");
            }
        }
        char first = p.charAt(0);
        if (first < '6' || first > '9') {             // chars compare by their character codes
            throw new InvalidPhoneNumberException("must start 6-9");
        }
        return p;
    }

    /** Letters and single spaces, 2-30 characters. Returns the name in Title Case. */
    public static String validateName(String name) throws InvalidNameException {
        String n = (name == null) ? "" : name.trim();
        if (n.length() < 2 || n.length() > 30) {
            throw new InvalidNameException("name must be 2-30 characters");
        }
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                throw new InvalidNameException("name may contain letters and spaces only ('" + c + "' found)");
            }
        }
        return MenuService.toTitleCase(n);
    }

    public Customer findByPhone(String phone) {
        for (int i = 0; i < count; i++) {
            if (customers[i].getPhone().equals(phone.trim())) {
                return customers[i];
            }
        }
        return null;
    }

    /** Creates a new profile. diet: 1 Any, 2 Veg only. */
    public Customer register(String phone, String name, int diet)
            throws InvalidPhoneNumberException, InvalidNameException, CustomerLimitException, InvalidInputException {
        String p = validatePhone(phone);
        if (findByPhone(p) != null) {
            throw new InvalidPhoneNumberException(p + " is already registered");
        }
        String n = validateName(name);
        if (diet != 1 && diet != 2) {
            throw new InvalidInputException("diet preference must be 1 Any or 2 Veg only");
        }
        if (count == CAPACITY) {
            throw new CustomerLimitException(CAPACITY + " customers already stored");
        }
        Customer c = (diet == 2) ? new Customer(p, n, true) : new Customer(p, n);   // overloaded constructors
        customers[count++] = c;
        return c;
    }

    /** Starts a visit for an existing customer (visit count +1, loyalty check). */
    public Customer checkIn(String phone) throws InvalidPhoneNumberException {
        String p = validatePhone(phone);
        Customer c = findByPhone(p);
        if (c == null) {
            throw new InvalidPhoneNumberException(p + " is not registered");
        }
        c.checkIn();
        return c;
    }

    /** Builds "Veg Biryani (11x) | Paneer Tikka (8x) | Masala Chai (6x)" with String.join. */
    public static String favouritesText(Customer c, MenuService menu) {
        int[] favs = c.topFavourites(3);
        if (favs.length == 0) {
            return "none yet";
        }
        String[] parts = new String[favs.length];
        for (int i = 0; i < favs.length; i++) {
            String dish;
            try {
                dish = menu.getByNumber(favs[i]).getName();
            } catch (ItemUnavailableException e) {
                dish = "Dish #" + favs[i];
            }
            parts[i] = dish + " (" + c.getDishCount(favs[i]) + "x)";
        }
        return String.join(" " + Colors.dot() + " ", parts);
    }

    public int getCount() { return count; }

    public Customer[] getAll() {
        Customer[] copy = new Customer[count];
        System.arraycopy(customers, 0, copy, 0, count);
        return copy;
    }

    /** Standalone demo of module M03. */
    public static void main(String[] args) throws Exception {
        CustomerService cs = new CustomerService();
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        System.out.println(Colors.title("== M03 Customer Profile & Favourites - standalone demo =="));
        try {
            cs.register("5123456780", "Test", 1);
        } catch (InvalidPhoneNumberException e) {
            System.out.println(Colors.err("InvalidPhoneNumberException: " + e.getMessage()));
        }
        try {
            cs.register("9876501234", "R2D2", 1);
        } catch (InvalidNameException e) {
            System.out.println(Colors.err("InvalidNameException: " + e.getMessage()));
        }
        Customer c = cs.register("9123456780", "priya sharma", 2);
        for (int visit = 1; visit <= 7; visit++) {
            c.checkIn();
            System.out.println("Visit #" + c.getVisits() + (c.isLoyaltyVisit() ? "  <- loyalty visit, 10% off" : ""));
        }
        c.addDish(3, 11);
        c.addDish(4, 8);
        c.addDish(11, 6);
        System.out.println("Favourites: " + favouritesText(c, menu));
    }
}
