import java.util.Arrays;

/**
 * Module M05 - Kitchen Queue (also triggers M11 token display).
 * Active orders are stored in an Order[] array. The SAME Order objects that OrderService
 * stores are referenced here, so there is never a second, conflicting copy of an order.
 * Run this file alone:  java Kitchen
 */
public class Kitchen {
    public static final int CAPACITY = 50;
    public static final int MINUTES_PER_ORDER_AHEAD = 2;

    private final Order[] queue = new Order[CAPACITY];
    private int count;
    private final AuthService auth;
    private int nextToken = 101;       // tokens are issued in sequence as orders become READY

    public Kitchen(AuthService auth) {
        this.auth = auth;
    }

    public void enqueue(Order order) throws OrderLimitExceededException {
        if (count == CAPACITY) {
            throw new OrderLimitExceededException("kitchen queue is full (" + CAPACITY + " active orders)");
        }
        queue[count++] = order;
    }

    /** Active orders sorted by priority: urgent first, then oldest first (Order.compareTo). */
    public Order[] getActiveSorted() {
        Order[] copy = Arrays.copyOf(queue, count);
        Arrays.sort(copy);
        return copy;
    }

    public Order findActive(int orderId) throws OrderNotFoundException {
        for (int i = 0; i < count; i++) {
            if (queue[i].getOrderId() == orderId) {
                return queue[i];
            }
        }
        throw new OrderNotFoundException("no active order #" + orderId);
    }

    /**
     * ETA = longest prep time of the order + 2 min x (orders ahead still cooking).
     * Orders ahead = orders placed before it in the priority list that are PLACED or PREPARING.
     */
    public int eta(Order order) {
        if (order.getStatus() >= Order.READY) {
            return 0;
        }
        int ahead = 0;
        for (Order o : getActiveSorted()) {
            if (o == order) {
                break;                                   // stop when we reach this order
            }
            if (o.getStatus() == Order.PLACED || o.getStatus() == Order.PREPARING) {
                ahead++;
            }
        }
        return order.getLongestPrep() + MINUTES_PER_ORDER_AHEAD * ahead;
    }

    /**
     * Moves an order to a target status. Only the NEXT stage is allowed (no skipping, no going back).
     * PREPARING and READY are set by a Chef; SERVED is set by a Waiter.
     * Returns a message; when the order becomes READY the message includes the token display.
     */
    public String changeStatus(int orderId, int target, Staff actor)
            throws OrderNotFoundException, InvalidStatusChangeException, UnauthorizedAccessException {
        Order o = findActive(orderId);
        int current = o.getStatus();
        if (target != current + 1 || target > Order.SERVED) {
            String next = current < Order.SERVED ? Order.statusName(current + 1) : "none";
            throw new InvalidStatusChangeException("cannot change #" + orderId + " from "
                    + Order.statusName(current) + " to " + Order.statusName(target)
                    + "; next allowed stage is " + next);
        }
        if (target == Order.SERVED) {
            AuthService.requireRole(actor, Staff.WAITER);
        } else {
            AuthService.requireRole(actor, Staff.CHEF);
        }
        if (target == Order.PREPARING && o.getChefName().isEmpty()) {
            o.setChefName(actor.getName());              // the chef who starts cooking is assigned
        }
        o.setStatus(target);
        String msg = "#" + orderId + " " + Colors.arrow() + " " + Order.statusName(target);
        if (target == Order.READY) {
            o.setToken(nextToken++);
            msg += " " + Colors.dot() + " Token " + o.getToken() + " " + Colors.dot() + " "
                    + (o.isDineIn() ? "Table " + o.getTableNo() + " buzzer " + Colors.bell() : "Takeaway counter");
            try {
                msg += "\n" + SevenSegment.render(String.valueOf(o.getToken()), SevenSegment.BOTH);
            } catch (InvalidTokenException | InvalidInputException e) {
                msg += "\n(token display error: " + e.getMessage() + ")";
            }
        }
        if (target == Order.SERVED) {
            remove(o);                                    // served orders leave the kitchen queue
        }
        return msg;
    }

    /** "Next stage" button: moves the order one step forward. */
    public String advance(int orderId, Staff actor)
            throws OrderNotFoundException, InvalidStatusChangeException, UnauthorizedAccessException {
        Order o = findActive(orderId);
        return changeStatus(orderId, o.getStatus() + 1, actor);
    }

    public void markUrgent(int orderId, Staff actor) throws OrderNotFoundException, UnauthorizedAccessException {
        AuthService.requireRole(actor, Staff.CHEF);
        findActive(orderId).setUrgent(true);
    }

    public Staff assignChef(int orderId, String chefName, Staff actor)
            throws OrderNotFoundException, InvalidChefException, UnauthorizedAccessException {
        AuthService.requireRole(actor, Staff.CHEF);
        Order o = findActive(orderId);
        Staff chef = auth.findChefByName(chefName);
        o.setChefName(chef.getName());
        return chef;
    }

    private void remove(Order o) {
        for (int i = 0; i < count; i++) {
            if (queue[i] == o) {
                for (int j = i; j < count - 1; j++) {     // shift the rest one place left
                    queue[j] = queue[j + 1];
                }
                queue[--count] = null;
                return;
            }
        }
    }

    /** Text view of the queue for the chef's screen. */
    public String display(String chefName) {
        StringBuilder sb = new StringBuilder();
        sb.append("-- KITCHEN QUEUE ").append(Colors.dot()).append(" Chef ").append(chefName).append(" --\n");
        Order[] sorted = getActiveSorted();
        if (sorted.length == 0) {
            sb.append("(no active orders)\n");
        }
        for (Order o : sorted) {
            sb.append(String.format("#%d %-8s %-6s %-9s ETA %2d min  chef: %s%n", o.getOrderId(),
                    o.isDineIn() ? "T" + o.getTableNo() : "Takeaway", o.isUrgent() ? "URGENT" : "Normal",
                    o.getStatusName(), eta(o), o.getChefName().isEmpty() ? "-" : o.getChefName()));
            for (OrderItem line : o.getLines()) {
                sb.append("     ").append(line).append("  [").append(line.getItem().kitchenStation()).append("]\n");
            }
            sb.append("     Spice: ").append(Order.spiceName(o.getSpiceLevel()));
            if (!o.getNote().isEmpty()) {
                sb.append("  Note: ").append(o.getNote());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public int getActiveCount() { return count; }

    /** Standalone demo of module M05 + M11 (orders are built directly, no keyboard input). */
    public static void main(String[] args) throws Exception {
        AuthService auth = new AuthService();
        Kitchen k = new Kitchen(auth);
        Staff chef = auth.findById("C201");
        Staff waiter = auth.findById("W104");
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        int[][] orders = {{2001, 6, 3}, {2002, 2, 7}, {2003, 7, 5}};   // {id, table, dish}
        for (int i = 0; i < orders.length; i++) {
            Order o = new Order(Order.DINE_IN, orders[i][1], null, "W104");
            o.addItem(menu.getByNumber(orders[i][2]), 1);
            o.place(orders[i][0], i + 1, 2, "", DateHelper.today(), DateHelper.currentHour());
            k.enqueue(o);
        }
        k.markUrgent(2002, chef);
        k.markUrgent(2003, chef);
        System.out.println(k.display(chef.getName()));
        try {
            k.changeStatus(2001, Order.SERVED, chef);
        } catch (InvalidStatusChangeException e) {
            System.out.println(Colors.err("InvalidStatusChangeException: " + e.getMessage()));
        }
        System.out.println(k.advance(2001, chef));
        System.out.println(k.advance(2001, chef));
        System.out.println(k.advance(2001, waiter));
    }
}
