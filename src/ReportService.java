import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Module M08 - Reports and Analytics.
 * Every number comes from the real records in memory: bills (M06), orders (M04),
 * feedback (M07), sensor alerts (M10) and the energy meter (M13). Nothing is hard-coded.
 * Run this file alone:  java ReportService
 */
public class ReportService {
    private final OrderService orders;
    private final BillingService billing;
    private final FeedbackService feedback;
    private final KitchenMonitor monitor;
    private final EnergyMeter energy;
    private final AuthService auth;
    private final MenuService menu;

    public ReportService(OrderService orders, BillingService billing, FeedbackService feedback,
                         KitchenMonitor monitor, EnergyMeter energy, AuthService auth, MenuService menu) {
        this.orders = orders;
        this.billing = billing;
        this.feedback = feedback;
        this.monitor = monitor;
        this.energy = energy;
        this.auth = auth;
        this.menu = menu;
    }

    public static void validateTopN(int n) throws InvalidInputException {
        if (n < 1 || n > 10) {
            throw new InvalidInputException("top N must be 1-10");
        }
    }

    /** All bills whose order date equals the given date. */
    public List<Bill> billsOn(String date) {
        List<Bill> result = new ArrayList<>();
        for (Bill b : billing.getAll()) {
            if (b.getDate().equals(date)) {
                result.add(b);
            }
        }
        return result;
    }

    private List<Bill> requireBills(String date) throws InvalidDateException, NoDataException {
        DateHelper.validate(date, DateHelper.today());
        List<Bill> bills = billsOn(date.trim());
        if (bills.isEmpty()) {
            throw new NoDataException("no billed orders on " + date.trim());
        }
        return bills;
    }

    /** Revenue = sum of grand totals of the bills (must equal the M06 bill totals). */
    public static double revenue(List<Bill> bills) {
        double total = 0;
        for (Bill b : bills) {
            total += b.getGrandTotal();
        }
        return Bill.round2(total);
    }

    /** dish counts indexed by item number, built from the billed orders. */
    private static int[] dishCounts(List<Bill> bills) {
        int[] counts = new int[Customer.DISH_SLOTS];
        for (Bill b : bills) {
            for (OrderItem line : b.getOrder().getLines()) {
                counts[line.getItem().getItemNo()] += line.getQuantity();
            }
        }
        return counts;
    }

    /**
     * Selection sort on parallel arrays (item numbers and counts), highest count first.
     * Returns the item numbers of the top n dishes that were actually sold.
     */
    public static int[] topIndices(int[] counts, int n) {
        int size = 0;
        int[] items = new int[counts.length];
        int[] values = new int[counts.length];
        for (int i = 1; i < counts.length; i++) {
            if (counts[i] > 0) {
                items[size] = i;
                values[size] = counts[i];
                size++;
            }
        }
        for (int i = 0; i < size - 1; i++) {                   // selection sort
            int maxPos = i;
            for (int j = i + 1; j < size; j++) {
                if (values[j] > values[maxPos]) {
                    maxPos = j;
                }
            }
            int tv = values[i]; values[i] = values[maxPos]; values[maxPos] = tv;   // swap
            int ti = items[i];  items[i] = items[maxPos];   items[maxPos] = ti;
        }
        int k = Math.min(n, size);
        int[] top = new int[k];
        for (int i = 0; i < k; i++) {
            top[i] = items[i];
        }
        return top;
    }

    private String dishName(int itemNo) {
        try {
            return menu.getByNumber(itemNo).getName();
        } catch (ItemUnavailableException e) {
            return "Dish #" + itemNo;
        }
    }

    // ---------------------------- Type 1: summary ----------------------------
    public String summary(String date) throws InvalidDateException, NoDataException {
        List<Bill> bills = requireBills(date);
        String d = date.trim();
        int[] counts = dishCounts(bills);
        int[] top = topIndices(counts, 4);
        String[] topText = new String[top.length];
        for (int i = 0; i < top.length; i++) {
            topText[i] = dishName(top[i]) + " " + counts[top[i]];
        }
        double avgToday = feedback.averageOn(d);
        boolean today = d.equals(DateHelper.today());
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAILY SUMMARY ").append(Colors.dot()).append(' ').append(d).append(" ===\n");
        sb.append(String.format("Revenue %s %s Orders %d %s Discounts %s %s GST %s%n", Colors.money(revenue(bills)),
                Colors.dot(), bills.size(), Colors.dot(), Colors.money(sumDiscount(bills)), Colors.dot(),
                Colors.money(sumTax(bills))));
        sb.append(String.format("Rating today %s %s Overall %.1f%s (%d reviews)%n",
                avgToday == 0 ? "no feedback yet" : String.format("%.1f%s", avgToday, Colors.star()), Colors.dot(),
                feedback.averageRating(), Colors.star(), feedback.reviewCount()));
        sb.append(String.format("Kitchen alerts %d %s Energy cost %s%n", monitor.countAlertsOn(d), Colors.dot(),
                today ? Colors.money(energy.getTotalCost()) + " (" + String.format("%.2f", energy.getTotalUnits())
                        + " kWh)" : "not recorded for past dates"));
        sb.append("Top: ").append(topText.length == 0 ? "-" : String.join(" " + Colors.dot() + " ", topText)).append('\n');
        sb.append("Items by category: ").append(categoryBreakdown(bills)).append('\n');
        sb.append("Payment modes: ").append(paymentModes(bills)).append('\n');
        return sb.toString();
    }

    private static double sumDiscount(List<Bill> bills) {
        return Bill.round2(bills.stream().mapToDouble(Bill::getDiscount).sum());
    }

    private static double sumTax(List<Bill> bills) {
        // reduce(): combines all values into one using the lambda (a + b)
        return Bill.round2(bills.stream().map(b -> b.getCgst() + b.getSgst()).reduce(0.0, (a, b) -> a + b));
    }

    /** Streams + groupingBy: quantity sold per category, e.g. "Main Course=2, Starters=2". */
    private static String categoryBreakdown(List<Bill> bills) {
        Map<String, Integer> byCategory = bills.stream()
                .flatMap(b -> java.util.Arrays.stream(b.getOrder().getLines()))
                .collect(Collectors.groupingBy(line -> line.getItem().getCategoryName(), TreeMap::new,
                        Collectors.summingInt(OrderItem::getQuantity)));
        return byCategory.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
    }

    /** Collectors.counting: how many bills per payment mode. */
    private static String paymentModes(List<Bill> bills) {
        Function<Bill, String> mode = Bill::getPaymentModeName;     // a Function stored in a variable
        Map<String, Long> m = bills.stream().collect(Collectors.groupingBy(mode, TreeMap::new, Collectors.counting()));
        return m.toString();
    }

    // ---------------------------- Type 2: top dishes ----------------------------
    public String topDishes(String date, int n) throws InvalidDateException, NoDataException, InvalidInputException {
        validateTopN(n);
        List<Bill> bills = requireBills(date);
        int[] counts = dishCounts(bills);
        int[] top = topIndices(counts, n);
        StringBuilder sb = new StringBuilder("=== TOP " + n + " DISHES " + Colors.dot() + " " + date.trim() + " ===\n");
        for (int i = 0; i < top.length; i++) {
            double avg = feedback.dishAverage(top[i]);
            sb.append(String.format("%2d. %-18s %3d sold  %s%n", i + 1, dishName(top[i]), counts[top[i]],
                    avg == 0 ? "" : String.format("rating %.1f%s", avg, Colors.star())));
        }
        if (top.length < n) {
            sb.append("(only ").append(top.length).append(" different dishes were sold)\n");
        }
        return sb.toString();
    }

    // ---------------------------- Type 3: hourly (2-D array) ----------------------------
    /**
     * Builds a 24 x 3 matrix: row = hour of day, columns = {orders, items, revenue}.
     * Row-wise processing prints each hour; column-wise processing adds the totals.
     */
    public double[][] hourlyMatrix(List<Bill> bills) {
        double[][] m = new double[24][3];
        for (Bill b : bills) {
            int h = b.getHour();
            m[h][0] += 1;
            m[h][1] += b.getOrder().getTotalQuantity();
            m[h][2] += b.getGrandTotal();
        }
        return m;
    }

    public String hourly(String date) throws InvalidDateException, NoDataException {
        List<Bill> bills = requireBills(date);
        double[][] m = hourlyMatrix(bills);
        double[] colTotals = new double[3];
        StringBuilder sb = new StringBuilder("=== HOURLY REPORT " + Colors.dot() + " " + date.trim() + " ===\n");
        sb.append(String.format("%-13s %6s %6s %12s%n", "Hour", "Orders", "Items", "Revenue"));
        for (int h = 0; h < m.length; h++) {
            if (m[h][0] == 0) {
                continue;                         // skip empty hours
            }
            sb.append(String.format("%02d:00-%02d:59   %6d %6d %12.2f%n", h, h, (int) m[h][0], (int) m[h][1], m[h][2]));
            for (int c = 0; c < 3; c++) {
                colTotals[c] += m[h][c];
            }
        }
        sb.append(String.format("%-13s %6d %6d %12.2f%n", "TOTAL", (int) colTotals[0], (int) colTotals[1],
                Bill.round2(colTotals[2])));
        return sb.toString();
    }

    // ---------------------------- Type 4: staff ----------------------------
    public String staffReport(String date) throws InvalidDateException, NoDataException {
        DateHelper.validate(date, DateHelper.today());
        String d = date.trim();
        // TreeMap = an ordered map: staff IDs come out sorted (A401, C201, K301, W104 ...)
        Map<String, int[]> stats = new TreeMap<>();           // value = {orders taken, orders cooked, bills}
        Map<String, Double> collected = new TreeMap<>();
        for (Staff s : auth.getAllStaff()) {
            stats.put(s.getStaffId(), new int[3]);
            collected.put(s.getStaffId(), 0.0);
        }
        boolean any = false;
        for (Order o : orders.getAll()) {
            if (!o.getDate().equals(d)) {
                continue;
            }
            any = true;
            stats.get(o.getWaiterId())[0]++;
            for (Staff s : auth.getAllStaff()) {
                if (s.getRole() == Staff.CHEF && s.getName().equals(o.getChefName())) {
                    stats.get(s.getStaffId())[1]++;
                }
            }
        }
        for (Bill b : billsOn(d)) {
            stats.get(b.getCashierId())[2]++;
            collected.put(b.getCashierId(), collected.get(b.getCashierId()) + b.getGrandTotal());
        }
        if (!any) {
            throw new NoDataException("no orders on " + d);
        }
        StringBuilder sb = new StringBuilder("=== STAFF REPORT " + Colors.dot() + " " + d + " ===\n");
        sb.append(String.format("%-5s %-8s %-8s %7s %7s %6s %12s%n", "ID", "Name", "Role", "Taken", "Cooked",
                "Bills", "Collected"));
        // Iterator: walk through the map entries one by one
        Iterator<Map.Entry<String, int[]>> it = stats.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, int[]> e = it.next();
            Staff s = auth.findById(e.getKey());
            int[] v = e.getValue();
            sb.append(String.format("%-5s %-8s %-8s %7d %7d %6d %12.2f%n", s.getStaffId(), s.getName(),
                    s.getRoleName(), v[0], v[1], v[2], collected.get(e.getKey())));
        }
        return sb.toString();
    }

    /** Standalone demo: an empty report and the date checks from the project document. */
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        TableRegister tables = new TableRegister();
        OrderService os = new OrderService(menu, tables, new Kitchen(auth));
        BillingService bs = new BillingService(os, tables, null);
        ReportService rs = new ReportService(os, bs, new FeedbackService(bs), new KitchenMonitor(),
                new EnergyMeter(), auth, menu);
        System.out.println(Colors.title("== M08 Reports & Analytics - standalone demo =="));
        String[] dates = {"31-09-2026", "29-02-2026", "2a-09-2026", "01-01-2099", DateHelper.today()};
        for (String d : dates) {
            try {
                System.out.println(rs.summary(d));
            } catch (InvalidDateException | NoDataException e) {
                System.out.println(Colors.err(d + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
    }
}
