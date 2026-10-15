import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Module M07 - Feedback and Ratings.
 * Feedback is accepted only for a genuine bill (verified with the M12 checksum) and only once per bill.
 * Running average: new average = (old total + rating) / (count + 1)
 * Run this file alone:  java FeedbackService
 */
public class FeedbackService {
    public static final int CAPACITY = 500;
    public static final int COMMENT_MAX = 100;

    private final Feedback[] feedback = new Feedback[CAPACITY];
    private int count;
    private final Set<String> billsWithFeedback = new HashSet<>();   // fast "already given?" check
    private int baselineCount;     // demo fixture only: reviews before this program run
    private int baselineTotal;
    private final int[] dishRatingTotal = new int[Customer.DISH_SLOTS];
    private final int[] dishRatingCount = new int[Customer.DISH_SLOTS];

    private final BillingService billing;

    public FeedbackService(BillingService billing) {
        this.billing = billing;
    }

    /** DEMO FIXTURE: earlier reviews (count and rating total) loaded with --demo. */
    public void loadBaseline(int reviews, int ratingTotal) {
        baselineCount = reviews;
        baselineTotal = ratingTotal;
    }

    /** Step 1: verify the bill and make sure no feedback exists yet. Returns the bill. */
    public Bill verifyForFeedback(String billId)
            throws InvalidBillFormatException, InvalidBillException, BillNotFoundException, DuplicateFeedbackException {
        Bill bill = billing.verify(billId);
        if (billsWithFeedback.contains(bill.getBillId())) {
            throw new DuplicateFeedbackException("feedback for " + bill.getBillId() + " was already given");
        }
        return bill;
    }

    public static void validateRating(int rating) throws InvalidRatingException {
        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException("must be 1-5 (you entered " + rating + ")");
        }
    }

    /** Step 2: store the ratings. dishRatings[i] belongs to the i-th line of the order. */
    public Feedback submit(String billId, int overall, int[] dishRatings, String comment)
            throws DineSyncException {
        Bill bill = verifyForFeedback(billId);
        validateRating(overall);
        OrderItem[] lines = bill.getOrder().getLines();
        if (dishRatings.length != lines.length) {
            throw new InvalidRatingException("expected " + lines.length + " dish ratings, got " + dishRatings.length);
        }
        for (int r : dishRatings) {
            validateRating(r);
        }
        String c = (comment == null) ? "" : comment.trim();
        if (c.length() > COMMENT_MAX) {
            throw new InvalidInputException("comment is " + c.length() + " characters; max " + COMMENT_MAX);
        }
        if (count == CAPACITY) {
            throw new InvalidInputException("feedback storage is full");
        }
        String[] names = new String[lines.length];
        for (int i = 0; i < lines.length; i++) {
            names[i] = lines[i].getItem().getName();
            int itemNo = lines[i].getItem().getItemNo();
            dishRatingTotal[itemNo] += dishRatings[i];
            dishRatingCount[itemNo]++;
        }
        Customer cust = bill.getOrder().getCustomer();
        Feedback f = new Feedback(bill.getBillId(), cust == null ? "Guest" : cust.getName(), overall, names,
                dishRatings.clone(), c);
        feedback[count++] = f;
        billsWithFeedback.add(bill.getBillId());
        return f;
    }

    /** Overall restaurant average including the baseline reviews. Returns 0 when there are none. */
    public double averageRating() {
        int total = baselineTotal;
        for (int i = 0; i < count; i++) {
            total += feedback[i].getOverall();
        }
        int n = reviewCount();
        return n == 0 ? 0.0 : (double) total / n;         // cast to double avoids integer division
    }

    public int reviewCount() { return baselineCount + count; }

    public double dishAverage(int itemNo) {
        if (itemNo < 1 || itemNo >= Customer.DISH_SLOTS || dishRatingCount[itemNo] == 0) {
            return 0.0;
        }
        return (double) dishRatingTotal[itemNo] / dishRatingCount[itemNo];
    }

    /** Average for this program run's feedback on a given date (bills of that date). */
    public double averageOn(String date) {
        List<Integer> ratings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            try {
                if (billing.verify(feedback[i].getBillId()).getDate().equals(date)) {
                    ratings.add(feedback[i].getOverall());         // autoboxing int -> Integer
                }
            } catch (DineSyncException e) {
                // stored IDs were verified on submit, so this cannot normally happen
            }
        }
        return ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    /** How many reviews gave 5, 4, 3 ... stars - TreeMap keeps the keys in order. */
    public Map<Integer, Long> ratingDistribution() {
        List<Feedback> list = getAllAsList();
        return list.stream().collect(Collectors.groupingBy(Feedback::getOverall, TreeMap::new, Collectors.counting()));
    }

    public List<Feedback> getAllAsList() {
        List<Feedback> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(feedback[i]);
        }
        return list;
    }

    public int getCount() { return count; }

    /** Standalone demo of M07 using a complete paid order. */
    public static void main(String[] args) throws Exception {
        AuthService auth = new AuthService();
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        TableRegister tables = new TableRegister();
        Kitchen kitchen = new Kitchen(auth);
        OrderService orders = new OrderService(menu, tables, kitchen);
        BillingService billing = new BillingService(orders, tables, null);
        FeedbackService fs = new FeedbackService(billing);
        fs.loadBaseline(214, 961);         // demo fixture: 214 earlier reviews averaging 4.49
        Staff waiter = auth.findById("W104");
        Staff chef = auth.findById("C201");
        Customer c = new Customer("9123456780", "Priya Sharma", true);
        c.checkIn();
        tables.allocate(6, 5);
        Order o = orders.startOrder(waiter, c, Order.DINE_IN, 6);
        orders.addItem(o, 3, 2);
        orders.addItem(o, 11, 2);
        orders.placeOrder(o, 2, "");
        kitchen.advance(o.getOrderId(), chef);
        kitchen.advance(o.getOrderId(), chef);
        kitchen.advance(o.getOrderId(), waiter);
        Bill b = billing.pay(auth.findById("K301"), o.getOrderId(), 1000.0, 1);
        System.out.println(Colors.title("== M07 Feedback & Ratings - standalone demo =="));
        try {
            fs.submit(b.getBillId(), 6, new int[]{5, 4}, "");
        } catch (InvalidRatingException e) {
            System.out.println(Colors.err("InvalidRatingException: " + e.getMessage()));
        }
        fs.submit(b.getBillId(), 5, new int[]{5, 4}, "Loved the biryani!");
        System.out.printf("Thank you, %s! Rating %.1f%s (%d reviews)%n", c.getName(), fs.averageRating(),
                Colors.star(), fs.reviewCount());
        try {
            fs.submit(b.getBillId(), 4, new int[]{4, 4}, "");
        } catch (DuplicateFeedbackException e) {
            System.out.println(Colors.err("DuplicateFeedbackException: " + e.getMessage()));
        }
    }
}
