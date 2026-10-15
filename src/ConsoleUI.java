import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The console screens (presentation layer). It reads input through InputHelper,
 * calls the services in Restaurant, and prints results. Business rules are NOT written
 * here; they live in the services, so the same rules apply in the console, the tests
 * and the standalone demos.
 */
public class ConsoleUI {
    private final Restaurant r;
    private final InputHelper in;
    private Staff current;                 // logged-in staff member (null = nobody)
    private Customer currentCustomer;      // customer being served by the waiter
    private int currentTable;              // table allocated for that customer (0 = none)

    public ConsoleUI(Restaurant restaurant, InputHelper input) {
        this.r = restaurant;
        this.in = input;
    }

    // ===================================== main menu =====================================
    public void run() {
        banner();
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println(Colors.title("=== DINESYNC MAIN MENU ==="));
            System.out.println("1 Staff login   2 Token display tool   3 Java concepts lab   0 Exit");
            int choice = in.readInt("Choice", 0, 3);
            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    tokenTool();
                    break;
                case 3:
                    ConceptShowcase.runAll(r);
                    break;
                default:                       // 0
                    running = false;
            }
        }
        System.out.println(Colors.ok("DineSync closed. (Data was kept in memory only and is now cleared.)"));
    }

    private void banner() {
        System.out.println(Colors.title("=================================================================="));
        System.out.println(Colors.title("  DineSync - Smart Restaurant Management with IoT Kitchen Monitoring"));
        System.out.println(Colors.title("=================================================================="));
        System.out.println("Academic Core Java prototype " + Colors.dot() + " in-memory arrays " + Colors.dot()
                + " simulated sensors");
    }

    // ===================================== M01 login =====================================
    private void login() {
        System.out.println("Roles: 1 Waiter " + Colors.dot() + " 2 Chef " + Colors.dot() + " 3 Cashier "
                + Colors.dot() + " 4 Admin");
        int role = in.readInt("Select role", 1, 4);
        String id;
        while (true) {
            id = in.readNonBlank("Staff ID");
            try {
                AuthService.validateStaffId(role, id);
                break;
            } catch (InvalidStaffIdException e) {
                System.out.println(Colors.err("InvalidStaffIdException: " + e.getMessage()));
            }
        }
        while (current == null) {
            String pin = in.readLine("PIN");
            try {
                current = r.auth.login(role, id, pin);
            } catch (InvalidPinException e) {
                System.out.println(Colors.err("InvalidPinException: " + e.getMessage()));
            } catch (InvalidStaffIdException | AccountLockedException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
                return;                         // back to the main menu
            }
        }
        System.out.println(Colors.ok("Welcome, " + current));
        r.files.appendAudit("LOGIN " + current.getStaffId());
        switch (current.getRole()) {
            case Staff.WAITER:
                waiterMenu();
                break;
            case Staff.CHEF:
                chefMenu();
                break;
            case Staff.CASHIER:
                cashierMenu();
                break;
            case Staff.ADMIN:
                adminMenu();
                break;
            default:
                System.out.println(Colors.err("Unknown role"));
        }
        System.out.println(Colors.ok(current.getName() + " logged out"));
        current = null;
    }

    // ===================================== Waiter =====================================
    private void waiterMenu() {
        int choice;
        do {
            System.out.println();
            System.out.println(Colors.title("--- WAITER MENU (" + current.getName() + ") ---")
                    + (currentCustomer == null ? "" : "  serving: " + currentCustomer.getName()
                    + (currentTable > 0 ? " at T" + currentTable : "")));
            System.out.println("1 Identify / register customer   2 Allocate table   3 View menu   4 Take order");
            System.out.println("5 Mark order SERVED   6 Customer feedback   7 Table register   8 Release unused table");
            System.out.println("9 Search menu   0 Logout");
            choice = in.readInt("Choice", 0, 9);
            switch (choice) {
                case 1: identifyCustomer(); break;
                case 2: allocateTable(); break;
                case 3: showMenuForCustomer(); break;
                case 4: takeOrder(); break;
                case 5: markServed(); break;
                case 6: collectFeedback(); break;
                case 7: showTables(); break;
                case 8: releaseUnusedTable(); break;
                case 9: searchMenu(); break;
                default: break;
            }
        } while (choice != 0);
    }

    /** M03: identify by mobile, or register a new profile. */
    private void identifyCustomer() {
        String phone;
        while (true) {
            phone = in.readNonBlank("Mobile");
            try {
                phone = CustomerService.validatePhone(phone);
                break;
            } catch (InvalidPhoneNumberException e) {
                System.out.println(Colors.err("InvalidPhoneNumberException: " + e.getMessage()));
            }
        }
        Customer c = r.customers.findByPhone(phone);
        try {
            if (c == null) {
                System.out.println("New customer.");
                String name;
                while (true) {
                    name = in.readNonBlank("Name");
                    try {
                        name = CustomerService.validateName(name);
                        break;
                    } catch (InvalidNameException e) {
                        System.out.println(Colors.err("InvalidNameException: " + e.getMessage()));
                    }
                }
                int diet = in.readInt("Diet preference 1 Any / 2 Veg only", 1, 2);
                c = r.customers.register(phone, name, diet);
                r.customers.checkIn(phone);
                System.out.println(Colors.ok("Profile created for " + c.getName() + " " + Colors.dot() + " visit #"
                        + c.getVisits()));
            } else {
                r.customers.checkIn(phone);
                System.out.println(Colors.paint(Colors.CYAN, Colors.star() + " Welcome back, " + c.getName()
                        + "! Visit #" + c.getVisits()));
            }
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            return;
        }
        if (c.isLoyaltyVisit()) {
            System.out.println(Colors.ok("Loyalty visit (" + c.getVisits() + " % 7 = 0) " + Colors.arrow()
                    + " 10% off this order"));
        }
        System.out.println("Preference: " + (c.isVegOnly() ? "Veg only" : "Any"));
        System.out.println("Favourites: " + CustomerService.favouritesText(c, r.menu));
        currentCustomer = c;
        currentTable = 0;
    }

    /** M09: suggest and allocate a table. */
    private void allocateTable() {
        int party = in.readInt("Party size", 1, 8);
        int suggestion;
        try {
            suggestion = r.tables.suggest(party);
        } catch (InvalidInputException e) {
            System.out.println(Colors.err(e.getMessage()));
            return;
        }
        System.out.println("Tables " + r.tables.toBinary() + " = " + r.tables.toHex() + " " + Colors.dot()
                + " Suggested: " + (suggestion == 0 ? "none free - try takeaway or wait" : "T" + suggestion));
        if (suggestion == 0) {
            return;
        }
        while (true) {
            int t = in.readInt("Table (0 = cancel)");
            if (t == 0) {
                return;
            }
            try {
                r.tables.allocate(t, party);
                System.out.println(Colors.ok("reg |= 1 << " + (TableRegister.TABLES - t) + " " + Colors.arrow()
                        + " " + r.tables.describe()));
                currentTable = t;
                return;
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
    }

    private void printMenu(MenuItem[] items) {
        for (MenuItem m : items) {
            System.out.println("  " + m);
        }
    }

    private void showMenuForCustomer() {
        if (currentCustomer == null) {
            System.out.println("(no customer selected - showing all available dishes)");
        }
        printMenu(r.menu.menuFor(currentCustomer));
    }

    private void searchMenu() {
        String k = in.readNonBlank("Keyword");
        List<MenuItem> found = r.menu.search(k);
        if (found.isEmpty()) {
            System.out.println("No dish matches '" + k + "'");
        }
        found.forEach(m -> System.out.println("  " + m));       // lambda passed to forEach (Consumer)
    }

    /** M04: build an order and send it to the kitchen (M05). */
    private void takeOrder() {
        if (currentCustomer == null) {
            System.out.println(Colors.err("Identify the customer first (option 1)"));
            return;
        }
        int type = in.readInt("Order type 1 Dine-in / 2 Takeaway", 1, 2);
        int table = 0;
        if (type == Order.DINE_IN) {
            table = currentTable;
            if (table == 0) {
                table = in.readInt("Table number", 1, 8);
            }
        }
        Order draft;
        try {
            draft = r.orders.startOrder(current, currentCustomer, type, table);
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            return;
        }
        System.out.println("Menu for " + currentCustomer.getName() + " (favourites first):");
        printMenu(r.menu.menuFor(currentCustomer));
        while (true) {
            int no = in.readInt("Dish no. (0 = finish)");
            if (no == 0) {
                if (draft.getLineCount() == 0) {
                    char again = in.readYesNo("Order is empty. Cancel this order?");
                    if (again == 'y') {
                        System.out.println("Order cancelled.");
                        return;
                    }
                    continue;
                }
                break;
            }
            int qty = in.readInt("Qty");
            try {
                OrderItem line = r.orders.addItem(draft, no, qty);
                System.out.println(Colors.ok(line + "   (order now " + Order.itemsText(draft.getTotalQuantity()) + ", "
                        + Colors.money(draft.getSubtotal()) + ")"));
            } catch (ItemUnavailableException | InvalidQuantityException | OrderLimitExceededException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        int spice = in.readInt("Spice 1 Mild / 2 Medium / 3 Hot", 1, 3);
        while (true) {
            String note = in.readLine("Kitchen note (optional, max 60)");
            try {
                Order placed = r.orders.placeOrder(draft, spice, note);
                System.out.println(Colors.ok("Order #" + placed.getOrderId() + " " + Colors.arrow() + " kitchen "
                        + Colors.dot() + " " + Order.itemsText(placed.getTotalQuantity()) + " " + Colors.dot() + " "
                        + Colors.money(placed.getSubtotal())));
                System.out.println(OrderService.summary(placed));
                System.out.println("Estimated ready in " + r.kitchen.eta(placed) + " min");
                r.files.appendAudit("ORDER #" + placed.getOrderId() + " by " + current.getStaffId());
                backgroundSensorCheck();
                currentCustomer = null;         // next customer must be identified again
                currentTable = 0;
                return;
            } catch (InvalidInputException e) {
                System.out.println(Colors.err("InvalidInputException: " + e.getMessage()));
            } catch (OrderLimitExceededException e) {
                System.out.println(Colors.err("OrderLimitExceededException: " + e.getMessage()));
                return;
            }
        }
    }

    /** M10 runs alongside the order flow: one quick scan; only alerts are printed. */
    private void backgroundSensorCheck() {
        int alerts = r.monitor.scanOnce(DateHelper.today(), DateHelper.nowTime(), line -> {
            if (line.contains("ALERT")) {
                System.out.println("[Background sensor check] " + line.trim());
            }
        });
        if (alerts == 0) {
            System.out.println("[Background sensor check] all 4 sensors OK");
        }
    }

    private void markServed() {
        Order[] active = r.kitchen.getActiveSorted();
        boolean any = false;
        for (Order o : active) {
            if (o.getStatus() == Order.READY) {
                System.out.println("  READY: " + o + " " + Colors.dot() + " token " + o.getToken());
                any = true;
            }
        }
        if (!any) {
            System.out.println("(no READY orders)");
        }
        int id = in.readInt("Order ID (0 = cancel)");
        if (id == 0) {
            return;
        }
        try {
            System.out.println(Colors.ok(r.kitchen.changeStatus(id, Order.SERVED, current)));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /** M07: feedback for a paid bill. */
    private void collectFeedback() {
        Bill bill;
        while (true) {
            String id = in.readLine("Bill ID (blank = cancel)");
            if (id.isEmpty()) {
                return;
            }
            try {
                bill = r.feedback.verifyForFeedback(id);
                System.out.println(Colors.ok(bill.getBillId() + " verified"));
                break;
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        int overall = readRating("Overall [1-5]");
        OrderItem[] lines = bill.getOrder().getLines();
        int[] dishRatings = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            dishRatings[i] = readRating(lines[i].getItem().getName());
        }
        String comment;
        while (true) {
            comment = in.readLine("Comment (optional, max 100)");
            if (comment.length() <= FeedbackService.COMMENT_MAX) {
                break;
            }
            System.out.println(Colors.err("comment is " + comment.length() + " characters; max 100"));
        }
        try {
            Feedback f = r.feedback.submit(bill.getBillId(), overall, dishRatings, comment);
            System.out.println(Colors.ok(String.format("Thank you, %s! Rating %.1f%s (%d)", f.getCustomerName(),
                    r.feedback.averageRating(), Colors.star(), r.feedback.reviewCount())));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private int readRating(String prompt) {
        while (true) {
            int v = in.readInt(prompt);
            try {
                FeedbackService.validateRating(v);
                return v;
            } catch (InvalidRatingException e) {
                System.out.println(Colors.err("InvalidRatingException: " + e.getMessage()));
            }
        }
    }

    private void showTables() {
        System.out.println("Register " + r.tables.describe() + " " + Colors.dot() + " busy: "
                + r.tables.occupiedCount() + "/8");
        System.out.print(r.tables.floorPlan());
    }

    private void releaseUnusedTable() {
        int t = in.readInt("Table to release (0 = cancel)");
        if (t == 0) {
            return;
        }
        try {
            r.orders.releaseUnusedTable(current, t);
            System.out.println(Colors.ok("T" + t + " released " + Colors.arrow() + " " + r.tables.describe()));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    // ===================================== Chef =====================================
    private void chefMenu() {
        int choice;
        do {
            System.out.println();
            System.out.println(Colors.title("--- CHEF MENU (" + current.getName() + ") ---"));
            System.out.println("1 View kitchen queue   2 Update an order   3 Run sensor scan   4 Alert log");
            System.out.println("5 Simulate overheat on next scan (demo)   0 Logout");
            choice = in.readInt("Choice", 0, 5);
            switch (choice) {
                case 1: System.out.print(r.kitchen.display(current.getName())); break;
                case 2: updateOrder(); break;
                case 3: runSensorScan(); break;
                case 4: showAlertLog(); break;
                case 5: simulateOverheat(); break;
                default: break;
            }
        } while (choice != 0);
    }

    /** M05: next stage / urgent / assign chef. */
    private void updateOrder() {
        System.out.print(r.kitchen.display(current.getName()));
        if (r.kitchen.getActiveCount() == 0) {
            return;
        }
        Order o;
        while (true) {
            int id = in.readInt("Order ID (0 = cancel)");
            if (id == 0) {
                return;
            }
            try {
                o = r.kitchen.findActive(id);
                break;
            } catch (OrderNotFoundException e) {
                System.out.println(Colors.err("OrderNotFoundException: " + e.getMessage()));
            }
        }
        int action = in.readInt("Action 1 Next stage / 2 Mark urgent / 3 Assign chef", 1, 3);
        try {
            if (action == 1) {
                System.out.println(Colors.ok(r.kitchen.advance(o.getOrderId(), current)));
            } else if (action == 2) {
                r.kitchen.markUrgent(o.getOrderId(), current);
                System.out.println(Colors.ok("#" + o.getOrderId() + " marked URGENT"));
            } else {
                String name = in.readNonBlank("Chef name");
                Staff chef = r.kitchen.assignChef(o.getOrderId(), name, current);
                System.out.println(Colors.ok("#" + o.getOrderId() + " assigned to Chef " + chef.getName()));
            }
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /** M10: full scan session. Limits are set by the Admin (Admin menu option 6). */
    private void runSensorScan() {
        int scans = in.readInt("Number of scans", 1, 50);
        int interval = in.readInt("Seconds between scans", 1, 10);
        try {
            r.monitor.runScans(scans, interval, DateHelper.today(), DateHelper.nowTime(), System.out::println);
        } catch (InvalidThresholdException e) {
            System.out.println(Colors.err("InvalidThresholdException: " + e.getMessage()));
        }
    }

    private void showAlertLog() {
        String[] log = r.monitor.getAlertLog();
        System.out.println("Alerts today: " + r.monitor.countAlertsOn(DateHelper.today()));
        for (String line : log) {
            System.out.println("  " + line);
        }
    }

    private void simulateOverheat() {
        double value = in.readDouble("Temperature reading to inject (e.g. 58.4)");
        Sensor s = r.monitor.getSensor(0);
        if (s instanceof AbstractSensor) {          // instanceof: check the real type before a downcast
            ((AbstractSensor) s).forceNextReading(value);
            System.out.println(Colors.warn("Next temperature reading will be " + value + " (simulated fault for demo)"));
        }
    }

    // ===================================== Cashier =====================================
    private void cashierMenu() {
        int choice;
        do {
            System.out.println();
            System.out.println(Colors.title("--- CASHIER MENU (" + current.getName() + ") ---"));
            System.out.println("1 Bill & pay an order   2 Verify a bill ID   3 Today's bills   0 Logout");
            choice = in.readInt("Choice", 0, 3);
            switch (choice) {
                case 1: billOrder(); break;
                case 2: verifyBill(); break;
                case 3: listBills(); break;
                default: break;
            }
        } while (choice != 0);
    }

    /** M06 + M12 + M09: bill, take payment, issue checksum bill ID, free the table. */
    private void billOrder() {
        for (Order o : r.orders.getAll()) {
            if (o.getStatus() == Order.SERVED) {
                System.out.println("  SERVED: #" + o.getOrderId() + " " + (o.isDineIn() ? "T" + o.getTableNo()
                        : "Takeaway") + " " + Colors.money(o.getSubtotal()));
            }
        }
        Order o;
        while (true) {
            int id = in.readInt("Order ID (0 = cancel)");
            if (id == 0) {
                return;
            }
            try {
                o = r.billing.billableOrder(id);
                break;
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        double[] a = BillingService.calculate(o);
        System.out.println("-------- BILL " + BillingService.makeBillId(o.getOrderId()) + " (preview) --------");
        System.out.printf("Item total %25.2f%n", a[0]);
        if (a[1] > 0) {
            System.out.printf("Loyalty discount (10%%) %13.2f%n", -a[1]);
        }
        System.out.printf("CGST 2.5%% %26.2f%nSGST 2.5%% %26.2f%n", a[2], a[3]);
        System.out.println("GRAND TOTAL " + Colors.money(a[4]));
        int mode = in.readInt("Mode 1 Cash / 2 UPI / 3 Card", 1, 3);
        double received = 0;
        String upiRef = "";
        if (mode == Bill.CASH) {
            while (true) {
                received = in.readDouble("Received");
                try {
                    BillingService.checkCash(a[4], received);
                    break;
                } catch (InsufficientPaymentException e) {
                    System.out.println(Colors.err("InsufficientPaymentException: " + e.getMessage()));
                }
            }
        } else if (mode == Bill.UPI) {
            upiRef = in.readNonBlank("UPI reference (12 digits)");
        }
        int split;
        while (true) {
            split = in.readInt("Split between how many people");
            try {
                BillingService.checkSplit(split);
                break;
            } catch (InvalidSplitException e) {
                System.out.println(Colors.err("InvalidSplitException: " + e.getMessage()));
            }
        }
        while (true) {
            try {
                Bill b;
                if (mode == Bill.CASH) {
                    b = r.billing.pay(current, o.getOrderId(), received, split);      // overloaded pay()
                } else if (mode == Bill.UPI) {
                    b = r.billing.pay(current, o.getOrderId(), upiRef, split);
                } else {
                    b = r.billing.pay(current, o.getOrderId(), split);
                }
                System.out.println(BillingService.receipt(b));
                System.out.println(Colors.ok("Bill ID " + b.getBillId() + " (" + BillingService.explain(o.getOrderId())
                        + ") " + Colors.dot() + " order BILLED"));
                if (o.isDineIn()) {
                    System.out.println(Colors.ok("T" + o.getTableNo() + " released " + Colors.arrow() + " "
                            + r.tables.describe()));
                }
                return;
            } catch (InvalidPaymentException e) {
                System.out.println(Colors.err("InvalidPaymentException: " + e.getMessage()));
                upiRef = in.readNonBlank("UPI reference (12 digits)");
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
                return;
            }
        }
    }

    /** M12: verify any bill ID. */
    private void verifyBill() {
        while (true) {
            String id = in.readLine("Bill ID (blank = back)");
            if (id.isEmpty()) {
                return;
            }
            try {
                Bill b = r.billing.verify(id);
                System.out.println(BillingService.explain(b.getBillNumber()) + " " + Colors.tick());
                System.out.println(Colors.ok("Genuine bill " + Colors.dot() + " " + Colors.money(b.getGrandTotal())
                        + " " + Colors.dot() + " PAID by " + b.getPaymentModeName()));
            } catch (DineSyncException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
    }

    private void listBills() {
        List<Bill> today = r.reports.billsOn(DateHelper.today());
        if (today.isEmpty()) {
            System.out.println("(no bills yet today)");
        }
        for (Bill b : today) {
            System.out.println("  " + b);
        }
        System.out.println("Total collected today: " + Colors.money(ReportService.revenue(today)));
    }

    // ===================================== Admin =====================================
    private void adminMenu() {
        int choice;
        do {
            System.out.println();
            System.out.println(Colors.title("--- ADMIN MENU (" + current.getName() + ") ---"));
            System.out.println("1 Add dish   2 Re-price dish   3 Dish availability   4 View menu (sorted)");
            System.out.println("5 Reports   6 Sensor limits & scan   7 Energy meter   8 Unlock staff account");
            System.out.println("9 Tables & alert log   10 Audit log (file)   11 Verify bill ID   0 Logout");
            choice = in.readInt("Choice", 0, 11);
            switch (choice) {
                case 1: addDish(); break;
                case 2: repriceDish(); break;
                case 3: toggleAvailability(); break;
                case 4: viewMenuSorted(); break;
                case 5: reports(); break;
                case 6: sensorSettings(); break;
                case 7: energyMenu(); break;
                case 8: unlockStaff(); break;
                case 9: showTables(); showAlertLog(); break;
                case 10: auditLog(); break;
                case 11: verifyBill(); break;
                default: break;
            }
        } while (choice != 0);
    }

    /** M02: add a dish; re-asks the field that failed, like the document's sample run. */
    private void addDish() {
        String name;
        while (true) {
            name = in.readNonBlank("Dish name");
            try {
                MenuService.validateName(name);
                if (r.menu.findByName(MenuService.toTitleCase(name)).isPresent()) {
                    throw new DuplicateDishException("\"" + r.menu.findByName(MenuService.toTitleCase(name)).get()
                            .getName() + "\" exists");
                }
                break;
            } catch (InvalidDishException | DuplicateDishException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        System.out.println("Category: 1 Starters 2 Main Course 3 Breads 4 Desserts 5 Beverages");
        int category = in.readInt("Category", 1, 5);
        int type = (category == MenuItem.CAT_BEVERAGES) ? MenuItem.TYPE_BEVERAGE
                : in.readInt("Type 1 Veg / 2 Non-Veg", 1, 2);
        double price;
        while (true) {
            price = in.readDouble("Price");
            try {
                MenuService.validatePrice(price);
                break;
            } catch (InvalidPriceException e) {
                System.out.println(Colors.err("InvalidPriceException: " + e.getMessage()));
            }
        }
        int prep = in.readInt("Prep time (minutes)", 1, 60);
        boolean available = in.readYesNo("Available") == 'y';
        try {
            MenuItem m = r.menu.addDish(current, name, category, type, price, prep, available);
            System.out.println(Colors.ok("Added " + m));
            r.files.appendAudit("MENU add " + m.getName());
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private void repriceDish() {
        printMenu(r.menu.getAll());
        int no = in.readInt("Dish no.");
        double price = in.readDouble("New price");
        try {
            r.menu.reprice(current, no, price);
            System.out.println(Colors.ok("Updated " + r.menu.getByNumber(no)));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private void toggleAvailability() {
        printMenu(r.menu.getAll());
        int no = in.readInt("Dish no.");
        boolean available = in.readYesNo("Available") == 'y';
        try {
            r.menu.setAvailability(current, no, available);
            System.out.println(Colors.ok("Updated " + r.menu.getByNumber(no)));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private void viewMenuSorted() {
        int how = in.readInt("Sort 1 By number / 2 By price / 3 By name", 1, 3);
        MenuItem[] items = (how == 1) ? r.menu.getAll() : (how == 2) ? r.menu.sortedByPrice() : r.menu.sortedByName();
        printMenu(items);
    }

    /** M08: reports, with optional export to a text file. */
    private void reports() {
        String date;
        while (true) {
            date = in.readLine("Date dd-mm-yyyy (blank = today " + DateHelper.today() + ")");
            if (date.isEmpty()) {
                date = DateHelper.today();
            }
            try {
                DateHelper.validate(date, DateHelper.today());
                break;
            } catch (InvalidDateException e) {
                System.out.println(Colors.err("InvalidDateException: " + e.getMessage()));
            }
        }
        int type = in.readInt("Type 1 Summary / 2 Top dishes / 3 Hourly / 4 Staff", 1, 4);
        String text;
        try {
            switch (type) {
                case 1:
                    text = r.reports.summary(date);
                    break;
                case 2:
                    text = r.reports.topDishes(date, in.readInt("Top N", 1, 10));
                    break;
                case 3:
                    text = r.reports.hourly(date);
                    break;
                default:
                    text = r.reports.staffReport(date);
            }
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            return;
        }
        System.out.print(text);
        if (in.readYesNo("Export this report to a text file") == 'y') {
            String[] names = {"", "summary", "top_dishes", "hourly", "staff"};
            try {
                Path p = r.files.writeReport(names[type] + "_" + date + ".txt", text);
                List<String> back = r.files.readLines(p);
                System.out.println(Colors.ok("Saved " + p.toAbsolutePath() + " (" + back.size() + " lines read back)"));
            } catch (IOException e) {
                System.out.println(Colors.err("Could not save report: " + e.getMessage()));
            }
        }
    }

    /** M10: thresholds set by the admin, then a scan session. */
    private void sensorSettings() {
        Sensor temp = r.monitor.getSensor(0);
        Sensor gas = r.monitor.getSensor(1);
        System.out.println("Current limits: temperature " + temp.getLimit() + " " + temp.getUnit() + ", gas "
                + gas.getLimit() + " ppm");
        while (true) {
            double v = in.readDouble("Temp limit (30-80)");
            try {
                temp.setLimit(v);
                break;
            } catch (InvalidThresholdException e) {
                System.out.println(Colors.err("InvalidThresholdException: " + e.getMessage()));
            }
        }
        while (true) {
            double v = in.readDouble("Gas limit (100-1000 ppm)");
            try {
                gas.setLimit(v);
                break;
            } catch (InvalidThresholdException e) {
                System.out.println(Colors.err("InvalidThresholdException: " + e.getMessage()));
            }
        }
        if (in.readYesNo("Run a scan now") == 'y') {
            runSensorScan();
        }
    }

    /** M13 energy meter sub-menu. */
    private void energyMenu() {
        int choice;
        do {
            System.out.println("Energy: 1 Add appliance   2 Energy report   3 Change slab tariff   0 Back");
            choice = in.readInt("Choice", 0, 3);
            if (choice == 1) {
                addAppliance();
            } else if (choice == 2) {
                System.out.print(r.energy.getCount() == 0 ? "(no appliances yet)\n" : r.energy.report());
            } else if (choice == 3) {
                double units = in.readDouble("Units in first slab");
                double r1 = in.readDouble("Rate for first slab");
                double r2 = in.readDouble("Rate for remaining units");
                try {
                    r.energy.setTariff(units, r1, r2);
                    System.out.println(Colors.ok("Tariff updated"));
                } catch (InvalidReadingException e) {
                    System.out.println(Colors.err("InvalidReadingException: " + e.getMessage()));
                }
            }
        } while (choice != 0);
    }

    private void addAppliance() {
        String name;
        while (true) {
            name = in.readNonBlank("Appliance");
            try { EnergyMeter.checkName(name); break; }
            catch (InvalidReadingException e) { System.out.println(Colors.err("InvalidReadingException: " + e.getMessage())); }
        }
        double v;
        while (true) {
            v = in.readDouble("V");
            try { EnergyMeter.checkVoltage(v); break; }
            catch (InvalidReadingException e) { System.out.println(Colors.err("InvalidReadingException: " + e.getMessage())); }
        }
        double i;
        while (true) {
            i = in.readDouble("I");
            try { EnergyMeter.checkCurrent(i); break; }
            catch (InvalidReadingException e) { System.out.println(Colors.err("InvalidReadingException: " + e.getMessage())); }
        }
        double pf;
        while (true) {
            pf = in.readDouble("PF");
            try { EnergyMeter.checkPowerFactor(pf); break; }
            catch (InvalidReadingException e) { System.out.println(Colors.err("InvalidReadingException: " + e.getMessage())); }
        }
        double h;
        while (true) {
            h = in.readDouble("Hours");
            try { EnergyMeter.checkHours(h); break; }
            catch (InvalidReadingException e) { System.out.println(Colors.err("InvalidReadingException: " + e.getMessage())); }
        }
        try {
            Appliance a = r.energy.addAppliance(name, v, i, pf, h);
            System.out.println(Colors.ok(String.format("%.2f kW %s %.2f kWh", a.getPowerKw(), Colors.arrow(),
                    a.getEnergyKwh())));
        } catch (InvalidReadingException | ApplianceLimitException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private void unlockStaff() {
        String id = in.readNonBlank("Staff ID to unlock");
        try {
            r.auth.unlock(current, id);
            System.out.println(Colors.ok(id.toUpperCase() + " unlocked"));
        } catch (DineSyncException e) {
            System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /** Reads audit.log back from disk (file input) and counts bills with a stream. */
    private void auditLog() {
        Path log = r.files.getOutputDir().resolve("audit.log");
        try {
            List<String> lines = r.files.readLines(log);
            int start = Math.max(0, lines.size() - 15);
            for (int i = start; i < lines.size(); i++) {
                System.out.println("  " + lines.get(i));
            }
            System.out.println("Lines: " + lines.size() + " " + Colors.dot() + " BILL lines: "
                    + r.files.countLinesContaining(log, "BILL"));
        } catch (java.nio.file.NoSuchFileException e) {
            System.out.println("No audit log yet (" + log + ")");
        } catch (IOException e) {
            System.out.println(Colors.err("Could not read audit log: " + e.getMessage()));
        }
    }

    // ===================================== M11 tool =====================================
    private void tokenTool() {
        while (true) {
            String token = in.readLine("Token (blank = back)");
            if (token.isEmpty()) {
                return;
            }
            try {
                SevenSegment.validateToken(token);
                int style = in.readInt("Style 1 ASCII / 2 Binary / 3 Both", 1, 3);
                System.out.print(SevenSegment.render(token, style));
            } catch (InvalidTokenException | InvalidInputException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
    }
}
