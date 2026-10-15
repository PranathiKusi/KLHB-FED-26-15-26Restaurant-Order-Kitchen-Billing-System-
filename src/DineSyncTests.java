import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated tests for all 13 modules plus integration and curriculum checks.
 * Plain Java (no JUnit needed): each check prints PASS or FAIL and a summary at the end.
 *
 *   javac *.java
 *   java DineSyncTests
 */
public class DineSyncTests {
    private static int passed;
    private static int failed;
    private static final List<String> failures = new ArrayList<>();
    private static Path tempDir;

    /** A functional interface for test code that may throw any exception. */
    interface Action {
        void run() throws Exception;
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  PASS " + name);
        } else {
            failed++;
            failures.add(name);
            System.out.println("  FAIL " + name);
        }
    }

    /** Passes when the action throws the expected exception type. */
    static void expect(String name, Class<? extends Throwable> expected, Action action) {
        try {
            action.run();
            check(name + " -> expected " + expected.getSimpleName() + " but nothing was thrown", false);
        } catch (Throwable t) {
            boolean ok = expected.isInstance(t);
            check(name + " -> " + t.getClass().getSimpleName() + ": " + t.getMessage(), ok);
        }
    }

    /** Passes when the action runs without any exception. */
    static void ok(String name, Action action) {
        try {
            action.run();
            check(name, true);
        } catch (Throwable t) {
            check(name + " -> unexpected " + t.getClass().getSimpleName() + ": " + t.getMessage(), false);
        }
    }

    static boolean near(double a, double b) {
        return Math.abs(a - b) < 0.005;
    }

    static Restaurant fresh() {
        Restaurant r = new Restaurant(tempDir.toString());
        r.menu.loadDefaultMenu();
        r.monitor.setFastMode(true);
        return r;
    }

    static Staff staff(Restaurant r, String id) {
        return r.auth.findById(id);
    }

    /** Places a served dine-in order for a customer; returns the order. */
    static Order servedOrder(Restaurant r, Customer c, int table, int[][] dishes) throws Exception {
        if (table > 0 && !r.tables.isOccupied(table)) {
            r.tables.allocate(table, 1);
        }
        Order o = r.orders.startOrder(staff(r, "W104"), c, table > 0 ? Order.DINE_IN : Order.TAKEAWAY, table);
        for (int[] d : dishes) {
            r.orders.addItem(o, d[0], d[1]);
        }
        r.orders.placeOrder(o, 2, "");
        r.kitchen.advance(o.getOrderId(), staff(r, "C201"));
        r.kitchen.advance(o.getOrderId(), staff(r, "C201"));
        r.kitchen.advance(o.getOrderId(), staff(r, "W104"));
        return o;
    }

    static Customer priya(Restaurant r) throws Exception {
        Customer c = r.customers.register("9123456780", "Priya Sharma", 2);
        c.loadHistory(13);
        r.customers.checkIn("9123456780");          // visit 14 -> loyalty
        return c;
    }

    // ============================================================================================
    public static void main(String[] args) throws Exception {
        Colors.setColor(false);
        tempDir = Files.createTempDirectory("dinesync-test");
        testLogin();
        testMenu();
        testCustomers();
        testOrders();
        testKitchen();
        testToken();
        testBilling();
        testChecksum();
        testFeedback();
        testTables();
        testSensors();
        testEnergy();
        testReports();
        testFiles();
        testIntegration();
        testCurriculum();
        System.out.println();
        System.out.println("==================================================");
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed, " + (passed + failed) + " total");
        for (String f : failures) {
            System.out.println("  failed: " + f);
        }
        System.out.println("==================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // -------------------------------------- M01 --------------------------------------
    static void testLogin() {
        System.out.println("\n[M01] Login & Access Control");
        AuthService a = new AuthService();
        ok("T1 W104 + correct PIN opens Waiter", () -> {
            if (a.login(1, "W104", "4821").getRole() != Staff.WAITER) throw new AssertionError();
        });
        ok("valid Chef / Cashier / Admin logins", () -> {
            a.login(2, "C201", "2468");
            a.login(3, "K301", "3690");
            a.login(4, "a401", "9999");                  // lower-case ID accepted
        });
        expect("T2 C104 while role = Waiter", InvalidStaffIdException.class, () -> a.login(1, "C104", "4821"));
        expect("ID wrong length W10", InvalidStaffIdException.class, () -> a.login(1, "W10", "4821"));
        expect("ID with letters W1X4", InvalidStaffIdException.class, () -> a.login(1, "W1X4", "4821"));
        expect("unknown ID W999", InvalidStaffIdException.class, () -> a.login(1, "W999", "4821"));
        expect("role 5 is outside 1-4 (unchecked)", InvalidMenuChoiceException.class, () -> a.login(5, "W104", "4821"));
        expect("wrong PIN", InvalidPinException.class, () -> a.login(1, "W105", "0000"));
        expect("2nd wrong PIN (letters)", InvalidPinException.class, () -> a.login(1, "W105", "12a4"));
        expect("T3 3rd wrong PIN locks", AccountLockedException.class, () -> a.login(1, "W105", "1111"));
        expect("locked account refuses even the correct PIN", AccountLockedException.class,
                () -> a.login(1, "W105", "1357"));
        expect("waiter cannot unlock", UnauthorizedAccessException.class,
                () -> a.unlock(a.findById("W104"), "W105"));
        ok("admin unlocks W105, then login works", () -> {
            a.unlock(a.findById("A401"), "W105");
            a.login(1, "W105", "1357");
        });
        ok("successful login resets the failure counter", () -> {
            try { a.login(2, "C202", "0000"); } catch (InvalidPinException e) { /* expected */ }
            a.login(2, "C202", "8642");
            if (a.findById("C202").getFailedAttempts() != 0) throw new AssertionError();
        });
    }

    // -------------------------------------- M02 --------------------------------------
    static void testMenu() {
        System.out.println("\n[M02] Menu Management");
        Restaurant r = fresh();
        Staff admin = staff(r, "A401");
        ok("T1 Mirchi Bajji Rs.120 stored as item #14", () -> {
            MenuItem m = r.menu.addDish(admin, "Mirchi Bajji", 1, 1, 120, 10, true);
            if (m.getItemNo() != 14 || !(m instanceof VegItem)) throw new AssertionError(m.toString());
        });
        expect("T2 'paneer tikka' duplicate", DuplicateDishException.class,
                () -> r.menu.addDish(admin, "paneer tikka", 1, 1, 220, 10, true));
        expect("T3 price 5", InvalidPriceException.class, () -> r.menu.addDish(admin, "Corn Soup", 1, 1, 5, 10, true));
        expect("price 2000.01", InvalidPriceException.class,
                () -> r.menu.addDish(admin, "Royal Thali", 2, 1, 2000.01, 10, true));
        ok("boundary prices 10 and 2000 accepted", () -> {
            r.menu.addDish(admin, "Papad", 1, 1, 10, 2, true);
            r.menu.addDish(admin, "Royal Thali", 2, 1, 2000, 30, true);
        });
        expect("name of 2 characters", InvalidDishException.class, () -> r.menu.addDish(admin, "Ab", 1, 1, 50, 5, true));
        expect("prep time 61", InvalidDishException.class, () -> r.menu.addDish(admin, "Slow Dish", 2, 1, 50, 61, true));
        expect("Beverage type with category Starters", InvalidDishException.class,
                () -> r.menu.addDish(admin, "Cold Coffee", 1, 3, 80, 5, true));
        expect("waiter cannot add dishes", UnauthorizedAccessException.class,
                () -> r.menu.addDish(staff(r, "W104"), "Tea Cake", 4, 1, 60, 5, true));
        ok("re-price Veg Biryani to 260", () -> {
            r.menu.reprice(admin, 3, 260);
            if (r.menu.getByNumber(3).getPrice() != 260) throw new AssertionError();
        });
        expect("chef cannot re-price", UnauthorizedAccessException.class, () -> r.menu.reprice(staff(r, "C201"), 3, 1));
        ok("unavailable dish hidden from the order menu", () -> {
            r.menu.setAvailability(admin, 9, false);
            for (MenuItem m : r.menu.menuFor(null)) {
                if (m.getItemNo() == 9) throw new AssertionError("Fish Curry still shown");
            }
        });
        check("search('biryani') finds 2 dishes", r.menu.search("biryani").size() == 2);
        check("search(category 5) finds the beverages", r.menu.search(5).size() == 2);
        check("sortedByPrice starts with Papad (10)", r.menu.sortedByPrice()[0].getName().equals("Papad"));
        check("findByName is case-insensitive (Optional)", r.menu.findByName("MASALA chai").isPresent());
        ok("menu fills to 30 then MenuFullException", () -> {
            int n = r.menu.getCount();
            for (int i = n; i < 30; i++) {
                r.menu.addDish(admin, "Dish " + (char) ('A' + i - n), 2, 1, 100, 10, true);
            }
            try {
                r.menu.addDish(admin, "One Too Many", 2, 1, 100, 10, true);
                throw new AssertionError("no MenuFullException");
            } catch (MenuFullException expected) {
                // correct
            }
        });
    }

    // -------------------------------------- M03 --------------------------------------
    static void testCustomers() {
        System.out.println("\n[M03] Customer Profile & Favourites");
        Restaurant r = fresh();
        expect("T1 5123456780 starts with 5", InvalidPhoneNumberException.class,
                () -> r.customers.register("5123456780", "Asha", 1));
        expect("9 digits", InvalidPhoneNumberException.class, () -> r.customers.register("912345678", "Asha", 1));
        expect("letters in number", InvalidPhoneNumberException.class, () -> r.customers.register("91234abc80", "Asha", 1));
        expect("name with digits", InvalidNameException.class, () -> r.customers.register("9000000001", "Asha 2", 1));
        expect("name of 1 letter", InvalidNameException.class, () -> r.customers.register("9000000001", "A", 1));
        ok("T2 new number -> profile created, visit #1", () -> {
            r.customers.register("9000000001", "asha rao", 1);
            Customer c = r.customers.checkIn("9000000001");
            if (c.getVisits() != 1 || !c.getName().equals("Asha Rao")) throw new AssertionError(c.toString());
        });
        expect("duplicate mobile", InvalidPhoneNumberException.class,
                () -> r.customers.register("9000000001", "Someone", 1));
        ok("loyalty on visits 7 and 14 only", () -> {
            Customer c = new Customer("9000000002", "Loyal Guest");
            String pattern = "";
            for (int v = 1; v <= 14; v++) {
                c.checkIn();
                pattern += c.isLoyaltyVisit() ? "L" : ".";
            }
            if (!pattern.equals("......L......L")) throw new AssertionError(pattern);
        });
        ok("T3 Priya visit #14 -> loyalty, veg-only menu, favourites first", () -> {
            Customer p = priya(r);
            p.addDish(3, 11);
            p.addDish(4, 8);
            p.addDish(11, 6);
            MenuItem[] m = r.menu.menuFor(p);
            if (!p.isLoyaltyVisit() || p.getVisits() != 14) throw new AssertionError("loyalty");
            if (m[0].getItemNo() != 3 || m[1].getItemNo() != 4 || m[2].getItemNo() != 11) throw new AssertionError("order");
            for (MenuItem item : m) {
                if (!item.isVeg()) throw new AssertionError("non-veg shown: " + item);
            }
        });
        ok("favourites text", () -> {
            String t = CustomerService.favouritesText(r.customers.findByPhone("9123456780"), r.menu);
            if (!t.startsWith("Veg Biryani (11x)")) throw new AssertionError(t);
        });
        ok("101st customer -> CustomerLimitException", () -> {
            CustomerService cs = new CustomerService();
            for (int i = 0; i < 100; i++) {
                cs.register(String.valueOf(9000000100L + i), "Guest", 1);
            }
            try {
                cs.register("9999999999", "Late Guest", 1);
                throw new AssertionError("no exception");
            } catch (CustomerLimitException expected) {
                // correct
            }
        });
    }

    // -------------------------------------- M04 --------------------------------------
    static void testOrders() throws Exception {
        System.out.println("\n[M04] Order Taking");
        Restaurant r = fresh();
        Staff admin = staff(r, "A401");
        Staff waiter = staff(r, "W104");
        r.menu.addDish(admin, "Mirchi Bajji", 1, 1, 120, 10, true);
        Customer p = priya(r);
        r.tables.allocate(6, 5);
        Order d = r.orders.startOrder(waiter, p, Order.DINE_IN, 6);
        expect("T1 veg-only customer picks Chicken 65", ItemUnavailableException.class, () -> r.orders.addItem(d, 7, 1));
        expect("T2 quantity 15", InvalidQuantityException.class, () -> r.orders.addItem(d, 4, 15));
        expect("quantity 0", InvalidQuantityException.class, () -> r.orders.addItem(d, 4, 0));
        expect("dish 99 does not exist", ItemUnavailableException.class, () -> r.orders.addItem(d, 99, 1));
        ok("T3 full valid order -> #2001, 6 items, Rs.920.00", () -> {
            r.orders.addItem(d, 3, 1);
            r.orders.addItem(d, 3, 1);                   // repeated selection is merged
            r.orders.addItem(d, 4, 1);
            r.orders.addItem(d, 14, 1);
            r.orders.addItem(d, 11, 2);
            Order o = r.orders.placeOrder(d, 2, "Less spicy, no onion in biryani");
            if (o.getOrderId() != 2001 || o.getTotalQuantity() != 6 || !near(o.getSubtotal(), 920))
                throw new AssertionError(o.toString());
            if (o.getLineCount() != 4) throw new AssertionError("merge failed: " + o.getLineCount() + " lines");
        });
        check("order status PLACED and in the kitchen queue", d.getStatus() == Order.PLACED
                && r.kitchen.getActiveCount() == 1);
        check("customer dish counts updated (Veg Biryani 2)", p.getDishCount(3) == 2);
        expect("second order on the same unpaid table", InvalidTableException.class,
                () -> r.orders.startOrder(waiter, p, Order.DINE_IN, 6));
        expect("dine-in on a table that was never allocated", InvalidTableException.class,
                () -> r.orders.startOrder(waiter, p, Order.DINE_IN, 1));
        expect("chef cannot take orders", UnauthorizedAccessException.class,
                () -> r.orders.startOrder(staff(r, "C201"), p, Order.TAKEAWAY, 0));
        Customer any = r.customers.register("9800000000", "Ravi Kumar", 1);
        r.customers.checkIn("9800000000");
        ok("takeaway order has no table", () -> {
            Order t = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            r.orders.addItem(t, 7, 2);                  // non-veg allowed for 'Any' diet
            r.orders.placeOrder(t, 3, "");
            if (t.getTableNo() != 0 || t.isDineIn()) throw new AssertionError();
        });
        ok("20-item limit: 21st item -> OrderLimitExceededException", () -> {
            Order big = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            r.orders.addItem(big, 1, 10);
            r.orders.addItem(big, 2, 10);
            try {
                r.orders.addItem(big, 6, 1);
                throw new AssertionError("no exception");
            } catch (OrderLimitExceededException expected) {
                // correct
            }
        });
        expect("merged quantity over 10 per dish", InvalidQuantityException.class, () -> {
            Order o = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            r.orders.addItem(o, 1, 6);
            r.orders.addItem(o, 1, 5);
        });
        expect("empty order cannot be placed", InvalidInputException.class,
                () -> r.orders.placeOrder(r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0), 2, ""));
        expect("kitchen note of 61 characters", InvalidInputException.class, () -> {
            Order o = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            r.orders.addItem(o, 1, 1);
            r.orders.placeOrder(o, 2, "x".repeat(61));
        });
        expect("spice level 4", InvalidInputException.class, () -> {
            Order o = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            r.orders.addItem(o, 1, 1);
            r.orders.placeOrder(o, 4, "");
        });
        ok("unavailable dish is rejected", () -> {
            r.menu.setAvailability(admin, 1, false);
            Order o = r.orders.startOrder(waiter, any, Order.TAKEAWAY, 0);
            try {
                r.orders.addItem(o, 1, 1);
                throw new AssertionError("accepted unavailable dish");
            } catch (ItemUnavailableException expected) {
                // correct
            }
        });
    }

    // -------------------------------------- M05 --------------------------------------
    static void testKitchen() throws Exception {
        System.out.println("\n[M05] Kitchen Queue");
        Restaurant r = fresh();
        Staff waiter = staff(r, "W104");
        Staff chef = staff(r, "C201");
        r.tables.loadState(0x4A);
        Customer p = priya(r);
        r.tables.allocate(6, 5);
        Order a = r.orders.startOrder(waiter, p, Order.DINE_IN, 6);
        r.orders.addItem(a, 3, 2);                       // Veg Biryani, prep 20
        r.orders.addItem(a, 11, 2);
        r.orders.placeOrder(a, 2, "");
        Customer g = r.customers.register("9800000001", "Guest One", 1);
        Order b = r.orders.startOrder(waiter, g, Order.DINE_IN, 2);
        r.orders.addItem(b, 7, 1);
        r.orders.placeOrder(b, 2, "");
        Order c = r.orders.startOrder(waiter, g, Order.DINE_IN, 7);
        r.orders.addItem(c, 5, 1);
        r.orders.placeOrder(c, 2, "");
        r.kitchen.markUrgent(b.getOrderId(), chef);
        r.kitchen.markUrgent(c.getOrderId(), chef);
        Order[] q = r.kitchen.getActiveSorted();
        check("queue: urgent #2002, urgent #2003, then normal #2001", q[0] == b && q[1] == c && q[2] == a);
        check("T1 #2001 with 2 orders ahead -> ETA 24 min", r.kitchen.eta(a) == 24);
        expect("T2 order 2010", OrderNotFoundException.class, () -> r.kitchen.advance(2010, chef));
        expect("T3 PLACED -> SERVED directly", InvalidStatusChangeException.class,
                () -> r.kitchen.changeStatus(2001, Order.SERVED, chef));
        expect("waiter cannot start cooking", UnauthorizedAccessException.class, () -> r.kitchen.advance(2001, waiter));
        expect("assign 'Kiran' (a waiter) as chef", InvalidChefException.class,
                () -> r.kitchen.assignChef(2001, "Kiran", chef));
        ok("assign chef Arjun", () -> {
            r.kitchen.assignChef(2001, "arjun", chef);
            if (!a.getChefName().equals("Arjun")) throw new AssertionError();
        });
        ok("PLACED -> PREPARING -> READY gives token 101 and display", () -> {
            r.kitchen.advance(2001, chef);
            String msg = r.kitchen.advance(2001, chef);
            if (a.getToken() != 101 || !msg.contains("0x30") || !msg.contains("Table 6")) throw new AssertionError(msg);
        });
        expect("reverse READY -> PREPARING", InvalidStatusChangeException.class,
                () -> r.kitchen.changeStatus(2001, Order.PREPARING, chef));
        expect("chef cannot mark SERVED", UnauthorizedAccessException.class, () -> r.kitchen.advance(2001, chef));
        ok("waiter marks SERVED; order leaves the queue", () -> {
            r.kitchen.advance(2001, waiter);
            if (a.getStatus() != Order.SERVED || r.kitchen.getActiveCount() != 2) throw new AssertionError();
        });
        check("READY order ETA is 0, cooking order ETA uses its own prep", r.kitchen.eta(b) == 15);
    }

    // -------------------------------------- M11 --------------------------------------
    static void testToken() {
        System.out.println("\n[M11] Seven-Segment Token Display");
        expect("T1 '1A7'", InvalidTokenException.class, () -> SevenSegment.validateToken("1A7"));
        expect("'1234' has 4 digits", InvalidTokenException.class, () -> SevenSegment.validateToken("1234"));
        expect("blank token", InvalidTokenException.class, () -> SevenSegment.validateToken(" "));
        ok("T2 '7' displayed as 007", () -> {
            if (!SevenSegment.validateToken("7").equals("007")) throw new AssertionError();
        });
        ok("T3 '107' codes 0x30 0x7E 0x70", () -> {
            String s = SevenSegment.render("107", SevenSegment.BINARY);
            if (!s.contains("1 = 0110000 (0x30)") || !s.contains("0 = 1111110 (0x7E)")
                    || !s.contains("7 = 1110000 (0x70)")) throw new AssertionError(s);
        });
        ok("ASCII '8' lights all 7 segments", () -> {
            String s = SevenSegment.render("8", SevenSegment.ASCII);
            if (!s.contains(" _ ") || !s.contains("|_|")) throw new AssertionError(s);
        });
        expect("style 4", InvalidInputException.class, () -> SevenSegment.render("1", 4));
    }

    // -------------------------------------- M06 --------------------------------------
    static void testBilling() throws Exception {
        System.out.println("\n[M06] Billing & Payment");
        Restaurant r = fresh();
        Staff cashier = staff(r, "K301");
        r.menu.addDish(staff(r, "A401"), "Mirchi Bajji", 1, 1, 120, 10, true);
        r.tables.loadState(0x4A);
        Customer p = priya(r);
        r.tables.allocate(6, 5);
        Order o = r.orders.startOrder(staff(r, "W104"), p, Order.DINE_IN, 6);
        r.orders.addItem(o, 14, 1);
        r.orders.addItem(o, 4, 1);
        r.orders.addItem(o, 3, 2);
        r.orders.addItem(o, 11, 2);
        r.orders.placeOrder(o, 2, "");
        expect("billing before SERVED", InvalidStatusChangeException.class, () -> r.billing.pay(cashier, 2001, 900.0, 2));
        r.kitchen.advance(2001, staff(r, "C201"));
        r.kitchen.advance(2001, staff(r, "C201"));
        r.kitchen.advance(2001, staff(r, "W104"));
        double[] a = BillingService.calculate(o);
        check("subtotal 920.00, discount 92.00", near(a[0], 920) && near(a[1], 92));
        check("CGST 20.70, SGST 20.70, total 869.40", near(a[2], 20.70) && near(a[3], 20.70) && near(a[4], 869.40));
        expect("T1 cash Rs.800 -> 69.40 due", InsufficientPaymentException.class, () -> r.billing.pay(cashier, 2001, 800.0, 2));
        check("failed payment keeps T6 busy and order SERVED", r.tables.isOccupied(6) && o.getStatus() == Order.SERVED);
        expect("T3 split 0", InvalidSplitException.class, () -> r.billing.pay(cashier, 2001, 900.0, 0));
        expect("split 9", InvalidSplitException.class, () -> r.billing.pay(cashier, 2001, 900.0, 9));
        expect("UPI reference with 11 digits", InvalidPaymentException.class,
                () -> r.billing.pay(cashier, 2001, "12345678901", 1));
        expect("UPI reference with letters", InvalidPaymentException.class,
                () -> r.billing.pay(cashier, 2001, "12345678901A", 1));
        expect("waiter cannot take payment", UnauthorizedAccessException.class,
                () -> r.billing.pay(staff(r, "W104"), 2001, 900.0, 2));
        ok("T2 cash Rs.900, split 2 -> change 30.60, 434.70 each, bill 2001-9", () -> {
            Bill b = r.billing.pay(cashier, 2001, 900.0, 2);
            if (!near(b.getChange(), 30.60) || !near(b.getPerPerson(), 434.70) || !b.getBillId().equals("2001-9"))
                throw new AssertionError(b.toString());
        });
        check("order BILLED and T6 released (0x4E -> 0x4A)", o.getStatus() == Order.BILLED
                && r.tables.getValue() == 0x4A);
        expect("billing the same order twice", InvalidStatusChangeException.class,
                () -> r.billing.pay(cashier, 2001, 900.0, 1));
        ok("UPI and card payments for takeaway orders", () -> {
            Customer g = r.customers.register("9800000002", "Guest Two", 1);
            Order t1 = servedOrder(r, g, 0, new int[][]{{7, 1}});
            Bill u = r.billing.pay(cashier, t1.getOrderId(), "123456789012", 1);
            Order t2 = servedOrder(r, g, 0, new int[][]{{6, 2}});
            Bill c = r.billing.pay(cashier, t2.getOrderId(), 3);
            if (u.getPaymentMode() != Bill.UPI || c.getPaymentMode() != Bill.CARD || !near(c.getChange(), 0))
                throw new AssertionError();
            if (!near(u.getGrandTotal(), 252.0)) throw new AssertionError("UPI total " + u.getGrandTotal());
        });
        check("receipt shows grand total line", BillingService.receipt(r.billing.getAll()[0]).contains("GRAND TOTAL"));
    }

    // -------------------------------------- M12 --------------------------------------
    static void testChecksum() throws Exception {
        System.out.println("\n[M12] Bill Checksum Security");
        check("recursive weightedSum(2001,1) = 9", BillingService.weightedSum(2001, 1) == 9);
        check("check digit of 2010 is 0", BillingService.checkDigit(2010) == 0);
        Restaurant r = fresh();
        Customer g = r.customers.register("9800000003", "Guest Three", 1);
        Order o = servedOrder(r, g, 0, new int[][]{{1, 1}});
        r.billing.pay(staff(r, "K301"), o.getOrderId(), 500.0, 1);
        ok("T1 2001-9 valid", () -> r.billing.verify("2001-9"));
        expect("T1 2001-8", InvalidBillException.class, () -> r.billing.verify("2001-8"));
        expect("T2 2010-9 swapped digits (expected 0)", InvalidBillException.class, () -> r.billing.verify("2010-9"));
        expect("T3 2010-0 checksum ok but no bill", BillNotFoundException.class, () -> r.billing.verify("2010-0"));
        expect("T3 2001/9 wrong format", InvalidBillFormatException.class, () -> r.billing.verify("2001/9"));
        expect("abcd-1 letters", InvalidBillFormatException.class, () -> r.billing.verify("abcd-1"));
        expect("2001-10 two check digits", InvalidBillFormatException.class, () -> r.billing.verify("2001-10"));
    }

    // -------------------------------------- M07 --------------------------------------
    static void testFeedback() throws Exception {
        System.out.println("\n[M07] Feedback & Ratings");
        Restaurant r = fresh();
        r.feedback.loadBaseline(214, 961);
        Customer p = priya(r);
        Order o = servedOrder(r, p, 6, new int[][]{{3, 2}, {4, 1}, {11, 2}, {10, 1}});
        Bill b = r.billing.pay(staff(r, "K301"), o.getOrderId(), "123456789012", 1);
        expect("T1 rating 6", InvalidRatingException.class, () -> r.feedback.submit(b.getBillId(), 6, new int[]{5, 4, 5, 4}, ""));
        expect("dish rating 0", InvalidRatingException.class, () -> r.feedback.submit(b.getBillId(), 5, new int[]{5, 0, 5, 4}, ""));
        expect("wrong number of dish ratings", InvalidRatingException.class,
                () -> r.feedback.submit(b.getBillId(), 5, new int[]{5, 4}, ""));
        expect("comment of 101 characters", InvalidInputException.class,
                () -> r.feedback.submit(b.getBillId(), 5, new int[]{5, 4, 5, 4}, "y".repeat(101)));
        expect("bill ID with a bad check digit", InvalidBillException.class,
                () -> r.feedback.submit("2001-8", 5, new int[]{5, 4, 5, 4}, ""));
        ok("T3 valid ratings -> average 4.5 (215 reviews)", () -> {
            r.feedback.submit(b.getBillId(), 5, new int[]{5, 4, 5, 4}, "Loved it!");
            if (r.feedback.reviewCount() != 215 || !String.format("%.1f", r.feedback.averageRating()).equals("4.5"))
                throw new AssertionError(r.feedback.averageRating() + " / " + r.feedback.reviewCount());
        });
        expect("T2 second feedback for 2001-9", DuplicateFeedbackException.class,
                () -> r.feedback.submit(b.getBillId(), 4, new int[]{4, 4, 4, 4}, ""));
        check("dish average Paneer Tikka = 4.0", near(r.feedback.dishAverage(4), 4.0));
        check("rating distribution {5=1}", r.feedback.ratingDistribution().toString().equals("{5=1}"));
    }

    // -------------------------------------- M09 --------------------------------------
    static void testTables() {
        System.out.println("\n[M09] Table Occupancy Register");
        TableRegister t = new TableRegister();
        t.loadState(0x4A);
        check("0x4A shown as 0100 1010", t.toBinary().equals("0100 1010") && t.toHex().equals("0x4A"));
        ok("party 5 -> suggested T6", () -> {
            if (t.suggest(5) != 6) throw new AssertionError();
        });
        expect("T1 party 5 at T3", TableCapacityException.class, () -> t.allocate(3, 5));
        expect("T2 T5 is busy", TableOccupiedException.class, () -> t.allocate(5, 5));
        expect("table 9", InvalidTableException.class, () -> t.allocate(9, 2));
        expect("party of 9", InvalidInputException.class, () -> t.allocate(8, 9));
        ok("T3 T6 then release: 0x4A -> 0x4E -> 0x4A", () -> {
            t.allocate(6, 5);
            if (t.getValue() != 0x4E || t.getValue() != 78) throw new AssertionError(t.describe());
            t.release(6);
            if (t.getValue() != 0x4A) throw new AssertionError(t.describe());
        });
        expect("release a free table", InvalidTableException.class, () -> t.release(1));
        ok("suggest returns 0 when nothing fits", () -> {
            TableRegister full = new TableRegister();
            full.loadState(0xFF);
            if (full.suggest(2) != 0 || full.occupiedCount() != 8) throw new AssertionError();
        });
        check("mask(1) = 128 (MSB), mask(8) = 1 (LSB)", TableRegister.mask(1) == 128 && TableRegister.mask(8) == 1);
    }

    // -------------------------------------- M10 --------------------------------------
    static void testSensors() {
        System.out.println("\n[M10] Kitchen Sensor Monitoring");
        KitchenMonitor km = new KitchenMonitor();
        km.setFastMode(true);
        expect("T1 temperature limit 120", InvalidThresholdException.class, () -> km.getSensor(0).setLimit(120));
        expect("temperature limit 29", InvalidThresholdException.class, () -> km.getSensor(0).setLimit(29));
        expect("gas limit 1001", InvalidThresholdException.class, () -> km.getSensor(1).setLimit(1001));
        ok("limits 55 C and 500 ppm accepted", () -> {
            km.getSensor(0).setLimit(55);
            km.getSensor(1).setLimit(500);
        });
        expect("0 scans", InvalidThresholdException.class, () -> km.runScans(0, 2, "01-01-2026", "10:00", s -> { }));
        expect("51 scans", InvalidThresholdException.class, () -> km.runScans(51, 2, "01-01-2026", "10:00", s -> { }));
        expect("interval 11 s", InvalidThresholdException.class, () -> km.runScans(1, 11, "01-01-2026", "10:00", s -> { }));
        ok("T2 58.4 C with limit 55 -> alert caught, scanning continues", () -> {
            List<String> out = new ArrayList<>();
            ((AbstractSensor) km.getSensor(0)).forceNextReading(58.4);
            ((AbstractSensor) km.getSensor(1)).forceNextReading(310);
            ((AbstractSensor) km.getSensor(2)).forceNextReading(18);
            ((AbstractSensor) km.getSensor(3)).forceNextReading(64);
            int alerts = km.runScans(1, 2, "01-01-2026", "19:30", out::add);
            if (alerts != 1 || out.size() != 6 || !out.get(1).contains("ALERT") || !out.get(4).contains("OK"))
                throw new AssertionError(out.toString());
        });
        ok("T3 all readings normal -> 4 x OK, no alerts", () -> {
            List<String> out = new ArrayList<>();
            double[] normal = {40, 300, 10, 60};
            for (int i = 0; i < 4; i++) {
                ((AbstractSensor) km.getSensor(i)).forceNextReading(normal[i]);
            }
            int alerts = km.scanOnce("01-01-2026", "19:31", out::add);
            long oks = out.stream().filter(s -> s.endsWith("OK")).count();
            if (alerts != 0 || oks != 4) throw new AssertionError(out.toString());
        });
        check("alert log has 1 entry for 01-01-2026", km.countAlertsOn("01-01-2026") == 1
                && km.getAlertLog()[0].contains("Temperature"));
        check("Sensor[] holds 4 different classes (polymorphism)", km.getSensors()[0] instanceof TemperatureSensor
                && km.getSensors()[1] instanceof GasLeakSensor && km.getSensors()[2] instanceof SmokeSensor
                && km.getSensors()[3] instanceof HumiditySensor);
        ok("random readings stay inside their simulated ranges", () -> {
            for (int i = 0; i < 200; i++) {
                double v = km.getSensor(0).read();
                if (v < 36 || v > 58) throw new AssertionError(v);
            }
        });
    }

    // -------------------------------------- M13 --------------------------------------
    static void testEnergy() {
        System.out.println("\n[M13] Kitchen Energy Meter");
        EnergyMeter m = new EnergyMeter();
        expect("T1 PF 1.4", InvalidReadingException.class, () -> m.addAppliance("Induction", 230, 9, 1.4, 4));
        expect("PF 0", InvalidReadingException.class, () -> m.addAppliance("Induction", 230, 9, 0, 4));
        expect("voltage 99", InvalidReadingException.class, () -> m.addAppliance("Induction", 99, 9, 0.9, 4));
        expect("current 25", InvalidReadingException.class, () -> m.addAppliance("Induction", 230, 25, 0.9, 4));
        expect("hours 25", InvalidReadingException.class, () -> m.addAppliance("Induction", 230, 9, 0.9, 25));
        expect("name 'X'", InvalidReadingException.class, () -> m.addAppliance("X", 230, 9, 0.9, 4));
        ok("T2 230 V x 8.5 A x 1.0 -> 1.955 kW, 6 h = 11.73 kWh", () -> {
            Appliance a = m.addAppliance("Tandoor", 230, 8.5, 1.0, 6);
            if (!near(a.getPowerKw(), 1.955) || !near(Bill.round2(a.getEnergyKwh()), 11.73)) throw new AssertionError();
        });
        ok("T3 all 4 appliances -> 26.70 kWh, Rs.173.60, Tandoor highest", () -> {
            m.addAppliance("Induction", 230, 9, 0.95, 4);
            m.addAppliance("Fridge", 230, 1.2, 0.85, 24);
            m.addAppliance("Fan", 230, 0.4, 0.8, 20);
            if (!near(m.getTotalUnits(), 26.70) || !near(m.getTotalCost(), 173.60)
                    || !m.highest().getName().equals("Tandoor"))
                throw new AssertionError(m.getTotalUnits() + " " + m.getTotalCost());
        });
        check("tariff boundary: 20 units = 120.00", near(m.cost(20), 120));
        check("tariff boundary: 20.01 units = 120.08", near(m.cost(20.01), 120.08));
        check("tariff: 0 units = 0, 10 units = 60", near(m.cost(0), 0) && near(m.cost(10), 60));
        ok("16th appliance -> ApplianceLimitException", () -> {
            for (int i = m.getCount(); i < 15; i++) {
                m.addAppliance("Light " + i, 230, 0.2, 1, 10);
            }
            try {
                m.addAppliance("Extra", 230, 1, 1, 1);
                throw new AssertionError("no exception");
            } catch (ApplianceLimitException expected) {
                // correct
            }
        });
    }

    // -------------------------------------- M08 --------------------------------------
    static void testReports() throws Exception {
        System.out.println("\n[M08] Reports & Analytics");
        Restaurant r = fresh();
        String today = DateHelper.today();
        expect("T1 31-09-2026", InvalidDateException.class, () -> DateHelper.validate("31-09-2026", "15-10-2026"));
        expect("29-02-2026 (not a leap year)", InvalidDateException.class,
                () -> DateHelper.validate("29-02-2026", "15-10-2026"));
        ok("29-02-2024 is valid (leap year)", () -> DateHelper.validate("29-02-2024", "15-10-2026"));
        expect("letters in the date", InvalidDateException.class, () -> DateHelper.validate("2a-09-2026", "15-10-2026"));
        expect("future date", InvalidDateException.class, () -> DateHelper.validate("16-10-2026", "15-10-2026"));
        expect("wrong format 2026-09-26", InvalidDateException.class, () -> DateHelper.validate("2026-09-26", "15-10-2026"));
        expect("month 13", InvalidDateException.class, () -> DateHelper.validate("01-13-2026", "15-10-2026"));
        expect("T2 date with no orders (empty report)", NoDataException.class, () -> r.reports.summary(today));
        expect("staff report with no orders", NoDataException.class, () -> r.reports.staffReport(today));
        Customer p = priya(r);
        Customer g = r.customers.register("9800000004", "Guest Four", 1);
        Order o1 = servedOrder(r, p, 6, new int[][]{{3, 2}, {4, 1}, {11, 2}});
        Order o2 = servedOrder(r, g, 0, new int[][]{{5, 1}, {3, 1}});
        Order o3 = servedOrder(r, g, 0, new int[][]{{11, 3}});
        Bill b1 = r.billing.pay(staff(r, "K301"), o1.getOrderId(), 1000.0, 1);
        Bill b2 = r.billing.pay(staff(r, "K301"), o2.getOrderId(), "123456789012", 2);
        // o3 is SERVED but not paid: it must NOT appear in revenue
        double expected = Bill.round2(b1.getGrandTotal() + b2.getGrandTotal());
        ok("T3 summary: revenue equals the sum of bill totals", () -> {
            String s = r.reports.summary(today);
            if (!s.contains(String.format("%.2f", expected)) || !s.contains("Orders 2")) throw new AssertionError(s);
        });
        check("unpaid order #" + o3.getOrderId() + " excluded from bills", r.reports.billsOn(today).size() == 2);
        ok("top dishes: Veg Biryani (3) first", () -> {
            String s = r.reports.topDishes(today, 3);
            if (!s.contains(" 1. Veg Biryani") || !s.contains("3 sold")) throw new AssertionError(s);
        });
        expect("top N = 0", InvalidInputException.class, () -> r.reports.topDishes(today, 0));
        expect("top N = 11", InvalidInputException.class, () -> r.reports.topDishes(today, 11));
        ok("hourly 2-D matrix totals match", () -> {
            double[][] m = r.reports.hourlyMatrix(r.reports.billsOn(today));
            double orders = 0;
            double revenue = 0;
            for (double[] row : m) {
                orders += row[0];
                revenue += row[2];
            }
            if (orders != 2 || !near(revenue, expected)) throw new AssertionError(orders + " " + revenue);
            if (!r.reports.hourly(today).contains("TOTAL")) throw new AssertionError();
        });
        ok("staff report: W104 took 3 orders, K301 billed 2", () -> {
            String s = r.reports.staffReport(today);
            if (!s.matches("(?s).*W104\\s+Kiran\\s+Waiter\\s+3\\s+0\\s+0.*")
                    || !s.matches("(?s).*K301\\s+Lakshmi\\s+Cashier\\s+0\\s+0\\s+2.*"))
                throw new AssertionError(s);
        });
        check("selection sort topIndices", java.util.Arrays.equals(ReportService.topIndices(new int[]{0, 2, 9, 0, 5}, 2),
                new int[]{2, 4}));
    }

    // -------------------------------------- File I/O --------------------------------------
    static void testFiles() throws Exception {
        System.out.println("\n[CO5] File I/O (FileStore)");
        FileStore fs = new FileStore(tempDir.toString());
        Path csv = tempDir.resolve("menu_test.csv");
        Files.write(csv, java.util.Arrays.asList("# comment", "Veg Biryani,2,1,250,20,y", "Broken line",
                "Bad Price,1,1,abc,5,y", "Too Cheap,1,1,5,5,y", "Masala Chai,5,3,40,5,y"));
        MenuService m = new MenuService();
        int n = fs.loadMenuCsv(csv, m);
        check("2 good records loaded, 3 malformed skipped", n == 2 && fs.getLoadMessages().size() == 3);
        FileStore fs2 = new FileStore(tempDir.toString());
        check("missing file handled", fs2.loadMenuCsv(tempDir.resolve("nope.csv"), new MenuService()) == 0
                && fs2.getLoadMessages().get(0).contains("not found"));
        ok("write report, read back, count lines via stream", () -> {
            Path p = fs.writeReport("t.txt", "a ALERT\nb\nc alert\n");
            if (fs.readLines(p).size() != 3 || fs.countLinesContaining(p, "alert") != 2) throw new AssertionError();
        });
        ok("audit log appends lines", () -> {
            fs.appendAudit("TEST one");
            fs.appendAudit("TEST two");
            if (fs.countLinesContaining(tempDir.resolve("audit.log"), "TEST") < 2) throw new AssertionError();
        });
        ok("repository data/menu.csv loads 13 dishes (when run from the project)", () -> {
            Path real = FileStore.findDataFile("menu.csv");
            if (Files.exists(real)) {
                MenuService ms = new MenuService();
                int count = new FileStore(tempDir.toString()).loadMenuCsv(real, ms);
                if (count != 13) throw new AssertionError(count);
            }
        });
    }

    // -------------------------------------- Integration --------------------------------------
    static void testIntegration() throws Exception {
        System.out.println("\n[INTEGRATION] End-to-end scenario (document section 5)");
        Restaurant r = fresh();
        r.tables.loadState(0x4A);
        r.feedback.loadBaseline(214, 961);
        Staff waiter = r.auth.login(1, "W104", "4821");
        Staff admin = r.auth.login(4, "A401", "9999");
        Staff chef = r.auth.login(2, "C201", "2468");
        Staff cashier = r.auth.login(3, "K301", "3690");
        Customer p = priya(r);
        p.addDish(3, 11);
        r.menu.addDish(admin, "Mirchi Bajji", 1, 1, 120, 10, true);
        check("new dish appears in the veg menu for Priya", java.util.Arrays.stream(r.menu.menuFor(p))
                .anyMatch(mi -> mi.getName().equals("Mirchi Bajji")));
        check("suggested table for 5 is T6", r.tables.suggest(5) == 6);
        r.tables.allocate(6, 5);
        Order o = r.orders.startOrder(waiter, p, Order.DINE_IN, 6);
        r.orders.addItem(o, 3, 2);
        r.orders.addItem(o, 4, 1);
        r.orders.addItem(o, 14, 1);
        r.orders.addItem(o, 11, 2);
        r.orders.placeOrder(o, 2, "Less spicy, no onion in biryani");
        ((AbstractSensor) r.monitor.getSensor(0)).forceNextReading(58.4);
        int alerts = r.monitor.scanOnce(DateHelper.today(), "19:30", s -> { });
        check("background sensor alert raised while order continues", alerts >= 1
                && r.kitchen.getActiveCount() == 1 && o.getStatus() == Order.PLACED);
        r.kitchen.advance(2001, chef);
        String ready = r.kitchen.advance(2001, chef);
        check("READY triggers the token display", ready.contains("Token 101") && ready.contains("0x30"));
        r.kitchen.advance(2001, waiter);
        Bill b = r.billing.pay(cashier, 2001, 900.0, 2);
        check("bill 2001-9, total 869.40, change 30.60, 434.70 each", b.getBillId().equals("2001-9")
                && near(b.getGrandTotal(), 869.40) && near(b.getChange(), 30.60) && near(b.getPerPerson(), 434.70));
        check("table register back to 0x4A", r.tables.getValue() == 0x4A);
        r.feedback.submit("2001-9", 5, new int[]{5, 4, 5, 4}, "Loved the new Mirchi Bajji!");
        check("rating 4.5 from 215 reviews", String.format("%.1f/%d", r.feedback.averageRating(),
                r.feedback.reviewCount()).equals("4.5/215"));
        r.energy.addAppliance("Tandoor", 230, 8.5, 1.0, 6);
        r.energy.addAppliance("Induction", 230, 9, 0.95, 4);
        r.energy.addAppliance("Fridge", 230, 1.2, 0.85, 24);
        r.energy.addAppliance("Fan", 230, 0.4, 0.8, 20);
        String s = r.reports.summary(DateHelper.today());
        check("daily report has revenue 869.40, 1 order, alerts, energy 173.60, Mirchi Bajji",
                s.contains("869.40") && s.contains("Orders 1") && s.contains("Kitchen alerts " + alerts)
                        && s.contains("173.60") && s.contains("Mirchi Bajji"));
        check("audit log recorded the bill", Files.readAllLines(tempDir.resolve("audit.log")).stream()
                .anyMatch(l -> l.contains("BILL 2001-9")));
    }

    // -------------------------------------- Curriculum checks --------------------------------------
    static void testCurriculum() {
        System.out.println("\n[CURRICULUM] OOP and language features");
        MenuItem v = new VegItem(1, "A Dish", 1, 100, 5, true);
        MenuItem n = new NonVegItem(2, "B Dish", 1, 100, 5, true);
        MenuItem b = new Beverage(3, "C Drink", 50, 2, true);
        MenuItem[] items = {n, b, v};
        check("overriding: getTypeLabel differs per subclass", v.getTypeLabel().equals("Veg")
                && n.getTypeLabel().equals("Non-Veg") && b.getTypeLabel().equals("Beverage"));
        check("runtime binding: kitchenStation chosen by object type", items[0].kitchenStation().equals("Non-veg station"));
        java.util.Arrays.sort(items);
        check("Comparable: Arrays.sort orders by name", items[0] == v && items[2] == b);
        check("inheritance: Beverage IS-A MenuItem, InvalidPinException IS-A Exception",
                b instanceof MenuItem && new InvalidPinException("x") instanceof Exception);
        check("checked vs unchecked: DineSyncException is not a RuntimeException",
                !(RuntimeException.class.isAssignableFrom(DineSyncException.class))
                        && RuntimeException.class.isAssignableFrom(InvalidMenuChoiceException.class));
        check("constructor overloading: Customer(phone, name) defaults to Any diet",
                !new Customer("9000000000", "X Y").isVegOnly() && new Customer("9000000000", "X Y", true).isVegOnly());
        check("toTitleCase uses split/substring/join", MenuService.toTitleCase("  mirchi   BAJJI ").equals("Mirchi Bajji"));
        check("Order implements Billable (interface reference)", (Billable) new Order(1, 1, null, "W104") != null);
        check("encapsulation: getLines returns a copy", new Order(2, 0, null, "W104").getLines().length == 0);
        check("leap year rule", DateHelper.isLeap(2024) && !DateHelper.isLeap(2100) && DateHelper.isLeap(2000));
    }
}
