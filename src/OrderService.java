/**
 * Module M04 - Order Taking.
 * Flow: startOrder() creates a draft -> addItem() repeatedly -> placeOrder() validates,
 * assigns the order ID, stores it in Order[] and sends it to the Kitchen queue.
 * Run this file alone:  java OrderService
 */
public class OrderService {
    public static final int CAPACITY = 500;
    public static final int NOTE_MAX = 60;
    public static final int FIRST_ORDER_ID = 2001;

    private final Order[] orders = new Order[CAPACITY];
    private int count;
    private int nextOrderId = FIRST_ORDER_ID;
    private long sequence;

    private final MenuService menu;
    private final TableRegister tables;
    private final Kitchen kitchen;

    public OrderService(MenuService menu, TableRegister tables, Kitchen kitchen) {
        this.menu = menu;
        this.tables = tables;
        this.kitchen = kitchen;
    }

    /** Starts a draft order. Dine-in needs a table that M09 has already allocated. */
    public Order startOrder(Staff waiter, Customer customer, int orderType, int tableNo)
            throws UnauthorizedAccessException, InvalidInputException, InvalidTableException {
        AuthService.requireRole(waiter, Staff.WAITER);
        if (customer == null) {
            throw new InvalidInputException("identify the customer first (M03)");
        }
        if (orderType != Order.DINE_IN && orderType != Order.TAKEAWAY) {
            throw new InvalidInputException("order type must be 1 Dine-in or 2 Takeaway");
        }
        if (orderType == Order.DINE_IN) {
            if (tableNo < 1 || tableNo > TableRegister.TABLES) {
                throw new InvalidTableException("table must be 1-8");
            }
            if (!tables.isOccupied(tableNo)) {
                throw new InvalidTableException("allocate T" + tableNo + " first (M09)");
            }
            if (hasOpenOrder(tableNo)) {
                throw new InvalidTableException("T" + tableNo + " already has an unpaid order");
            }
        }
        return new Order(orderType, orderType == Order.DINE_IN ? tableNo : 0, customer, waiter.getStaffId());
    }

    /** Adds a dish to a draft, enforcing availability and the customer's diet preference. */
    public OrderItem addItem(Order draft, int itemNo, int qty)
            throws ItemUnavailableException, InvalidQuantityException, OrderLimitExceededException {
        MenuItem item = menu.getByNumber(itemNo);
        if (!item.isAvailable()) {
            throw new ItemUnavailableException(item.getName() + " is not available today");
        }
        Customer c = draft.getCustomer();
        if (c.isVegOnly() && !item.isVeg()) {
            throw new ItemUnavailableException(item.getName() + " is non-veg (Veg only customer)");
        }
        draft.addItem(item, qty);
        for (OrderItem line : draft.getLines()) {
            if (line.getItem() == item) {
                return line;
            }
        }
        return null;
    }

    /** Validates spice + note, assigns the ID, stores the order and sends it to the kitchen. */
    public Order placeOrder(Order draft, int spiceLevel, String note)
            throws InvalidInputException, OrderLimitExceededException {
        if (draft.getLineCount() == 0) {
            throw new InvalidInputException("order is empty - add at least one dish");
        }
        if (spiceLevel < 1 || spiceLevel > 3) {
            throw new InvalidInputException("spice level must be 1 Mild, 2 Medium or 3 Hot");
        }
        String n = (note == null) ? "" : note.trim();
        if (n.length() > NOTE_MAX) {
            throw new InvalidInputException("kitchen note is " + n.length() + " characters; max " + NOTE_MAX);
        }
        if (count == CAPACITY) {
            throw new OrderLimitExceededException("order storage full (" + CAPACITY + ")");
        }
        draft.place(nextOrderId, ++sequence, spiceLevel, n, DateHelper.today(), DateHelper.currentHour());
        kitchen.enqueue(draft);                 // may throw if the kitchen is full -> nothing stored yet
        nextOrderId++;
        orders[count++] = draft;
        for (OrderItem line : draft.getLines()) {
            draft.getCustomer().addDish(line.getItem().getItemNo(), line.getQuantity());
        }
        return draft;
    }

    public Order findOrder(int orderId) throws OrderNotFoundException {
        for (int i = 0; i < count; i++) {
            if (orders[i].getOrderId() == orderId) {
                return orders[i];
            }
        }
        throw new OrderNotFoundException("no order #" + orderId);
    }

    /** True when a table has an order that is not yet BILLED. */
    public boolean hasOpenOrder(int tableNo) {
        for (int i = 0; i < count; i++) {
            Order o = orders[i];
            if (o.isDineIn() && o.getTableNo() == tableNo && o.getStatus() != Order.BILLED) {
                return true;
            }
        }
        return false;
    }

    /** Releases a table that was allocated but never used for an order (party left). */
    public void releaseUnusedTable(Staff waiter, int tableNo)
            throws UnauthorizedAccessException, InvalidTableException {
        AuthService.requireRole(waiter, Staff.WAITER);
        if (hasOpenOrder(tableNo)) {
            throw new InvalidTableException("T" + tableNo + " has an unpaid order; it is released after payment");
        }
        tables.release(tableNo);
    }

    /** Text receipt of a placed order. */
    public static String summary(Order o) {
        StringBuilder sb = new StringBuilder();
        for (OrderItem line : o.getLines()) {
            sb.append(String.format("  %-18s x%-2d %10s%n", line.getItem().getName(), line.getQuantity(),
                    Colors.money(line.getLineTotal())));
        }
        sb.append(String.format("  Order #%d %s %s %s %s %s Spice: %s", o.getOrderId(), Colors.dot(),
                Order.itemsText(o.getTotalQuantity()), Colors.dot(), Colors.money(o.getSubtotal()), Colors.dot(),
                Order.spiceName(o.getSpiceLevel())));
        if (!o.getNote().isEmpty()) {
            sb.append(String.format("%n  Note: %s", o.getNote()));
        }
        return sb.toString();
    }

    public Order[] getAll() {
        Order[] copy = new Order[count];
        System.arraycopy(orders, 0, copy, 0, count);
        return copy;
    }

    public int getCount() { return count; }

    /** Standalone demo of module M04 using the Priya Sharma example from the project document. */
    public static void main(String[] args) throws Exception {
        AuthService auth = new AuthService();
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        Staff admin = auth.findById("A401");
        menu.addDish(admin, "Mirchi Bajji", 1, 1, 120, 10, true);
        TableRegister tables = new TableRegister();
        Kitchen kitchen = new Kitchen(auth);
        OrderService os = new OrderService(menu, tables, kitchen);
        Customer priya = new Customer("9123456780", "Priya Sharma", true);
        priya.checkIn();
        tables.allocate(6, 5);
        Staff waiter = auth.findById("W104");
        System.out.println(Colors.title("== M04 Order Taking - standalone demo =="));
        Order draft = os.startOrder(waiter, priya, Order.DINE_IN, 6);
        int[][] picks = {{3, 2}, {7, 1}, {4, 15}, {4, 1}, {14, 1}, {11, 2}};
        for (int[] p : picks) {
            try {
                OrderItem line = os.addItem(draft, p[0], p[1]);
                System.out.println(Colors.ok(line.toString()));
            } catch (ItemUnavailableException | InvalidQuantityException | OrderLimitExceededException e) {
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        Order placed = os.placeOrder(draft, 2, "Less spicy, no onion in biryani");
        System.out.println(Colors.ok("Order #" + placed.getOrderId() + " " + Colors.arrow() + " kitchen"));
        System.out.println(summary(placed));
    }
}
