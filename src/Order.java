

/**
 * An order (modules M04 / M05).
 * Status values are int constants: PLACED -> PREPARING -> READY -> SERVED -> BILLED.
 * Implements Billable (subtotal, discount) and Comparable (kitchen priority).
 */
public class Order implements Billable, Comparable<Order> {
    public static final int PLACED = 0;
    public static final int PREPARING = 1;
    public static final int READY = 2;
    public static final int SERVED = 3;
    public static final int BILLED = 4;
    private static final String[] STATUS_NAMES = {"PLACED", "PREPARING", "READY", "SERVED", "BILLED"};

    public static final int DINE_IN = 1;
    public static final int TAKEAWAY = 2;
    public static final int MAX_ITEMS = 20;        // total quantity per order
    public static final int MAX_QTY_PER_DISH = 10;
    private static final String[] SPICE_NAMES = {"", "Mild", "Medium", "Hot"};

    private int orderId;                 // 0 until the order is placed
    private final int orderType;
    private final int tableNo;           // 0 for takeaway
    private final Customer customer;
    private final String waiterId;
    private final OrderItem[] lines = new OrderItem[MAX_ITEMS];
    private int lineCount;
    private int spiceLevel = 2;
    private String note = "";
    private int status = PLACED;
    private boolean urgent;
    private long sequence;               // increases with every order: smaller = older
    private String chefName = "";
    private String date = "";            // dd-mm-yyyy
    private int hour;
    private boolean loyaltyApplied;
    private int token;

    public Order(int orderType, int tableNo, Customer customer, String waiterId) {
        this.orderType = orderType;
        this.tableNo = tableNo;
        this.customer = customer;
        this.waiterId = waiterId;
        this.loyaltyApplied = customer != null && customer.isLoyaltyVisit();
    }

    /**
     * Adds a dish. If the dish is already in the order, its quantity is increased (merge).
     * Throws when the quantity per dish goes outside 1-10 or the order would pass 20 items.
     */
    public void addItem(MenuItem item, int qty) throws InvalidQuantityException, OrderLimitExceededException {
        if (qty < 1 || qty > MAX_QTY_PER_DISH) {
            throw new InvalidQuantityException("qty must be 1-" + MAX_QTY_PER_DISH + " (you entered " + qty + ")");
        }
        if (getTotalQuantity() + qty > MAX_ITEMS) {
            throw new OrderLimitExceededException("order already has " + getTotalQuantity()
                    + " items; adding " + qty + " would pass the limit of " + MAX_ITEMS);
        }
        OrderItem existing = findLine(item.getItemNo());
        if (existing != null) {
            if (existing.getQuantity() + qty > MAX_QTY_PER_DISH) {
                throw new InvalidQuantityException(item.getName() + " would become x"
                        + (existing.getQuantity() + qty) + "; max " + MAX_QTY_PER_DISH + " per dish");
            }
            existing.addQuantity(qty);
        } else {
            lines[lineCount] = new OrderItem(item, qty);
            lineCount++;
        }
    }

    private OrderItem findLine(int itemNo) {
        for (int i = 0; i < lineCount; i++) {
            if (lines[i].getItem().getItemNo() == itemNo) {
                return lines[i];
            }
        }
        return null;     // null reference: "no object found"
    }

    public int getTotalQuantity() {
        int total = 0;
        for (int i = 0; i < lineCount; i++) {
            total += lines[i].getQuantity();
        }
        return total;
    }

    /** Longest preparation time among the dishes (used in the ETA formula). */
    public int getLongestPrep() {
        int max = 0;
        for (int i = 0; i < lineCount; i++) {
            max = Math.max(max, lines[i].getItem().getPrepMinutes());
        }
        return max;
    }

    @Override
    public double getSubtotal() {
        double sum = 0;
        for (int i = 0; i < lineCount; i++) {
            sum += lines[i].getLineTotal();
        }
        return Bill.round2(sum);
    }

    @Override
    public double getDiscount() {
        return loyaltyApplied ? Bill.round2(getSubtotal() * LOYALTY_RATE) : 0.0;
    }

    /** Kitchen priority: urgent orders first, then older orders first. */
    @Override
    public int compareTo(Order other) {
        if (this.urgent != other.urgent) {
            return this.urgent ? -1 : 1;
        }
        return Long.compare(this.sequence, other.sequence);
    }

    public static String statusName(int status) {
        return (status >= PLACED && status <= BILLED) ? STATUS_NAMES[status] : "UNKNOWN";
    }

    /** "1 item" / "6 items" */
    public static String itemsText(int n) {
        return n + (n == 1 ? " item" : " items");
    }

    public static String spiceName(int level) {
        return (level >= 1 && level <= 3) ? SPICE_NAMES[level] : "?";
    }

    /** Returns a copy of the filled part of the array so callers cannot change our array. */
    public OrderItem[] getLines() {
        OrderItem[] copy = new OrderItem[lineCount];
        for (int i = 0; i < lineCount; i++) {
            copy[i] = lines[i];
        }
        return copy;
    }

    public int getLineCount() { return lineCount; }
    public int getOrderId() { return orderId; }
    public int getOrderType() { return orderType; }
    public boolean isDineIn() { return orderType == DINE_IN; }
    public int getTableNo() { return tableNo; }
    public Customer getCustomer() { return customer; }
    public String getWaiterId() { return waiterId; }
    public int getSpiceLevel() { return spiceLevel; }
    public String getNote() { return note; }
    public int getStatus() { return status; }
    public String getStatusName() { return statusName(status); }
    public boolean isUrgent() { return urgent; }
    public long getSequence() { return sequence; }
    public String getChefName() { return chefName; }
    public String getDate() { return date; }
    public int getHour() { return hour; }
    public boolean isLoyaltyApplied() { return loyaltyApplied; }
    public int getToken() { return token; }

    // Mutators used by the services after they have validated the change
    public void place(int orderId, long sequence, int spiceLevel, String note, String date, int hour) {
        this.orderId = orderId;
        this.sequence = sequence;
        this.spiceLevel = spiceLevel;
        this.note = note;
        this.date = date;
        this.hour = hour;
        this.status = PLACED;
    }

    public void setStatus(int status) { this.status = status; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }
    public void setChefName(String chefName) { this.chefName = chefName; }
    public void setToken(int token) { this.token = token; }

    @Override
    public String toString() {
        String where = isDineIn() ? "T" + tableNo : "Takeaway";
        return "#" + orderId + " " + where + " " + (urgent ? "URGENT" : "Normal") + " " + getStatusName()
                + " " + itemsText(getTotalQuantity());
    }
}
