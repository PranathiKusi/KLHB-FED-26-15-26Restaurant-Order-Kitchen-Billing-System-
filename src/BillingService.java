import java.util.Arrays;

/**
 * Module M06 - Billing and Payment, and Module M12 - Bill Checksum Security.
 * A Bill object is created only after the payment is valid, then the order becomes BILLED
 * and its table is released in the TableRegister (M09).
 * Run this file alone:  java BillingService
 */
public class BillingService {
    public static final int CAPACITY = 500;

    private final Bill[] bills = new Bill[CAPACITY];
    private final int[] billNumbers = new int[CAPACITY];   // stays sorted because order IDs only increase
    private int count;

    private final OrderService orders;
    private final TableRegister tables;
    private final FileStore files;      // may be null (no audit log)

    public BillingService(OrderService orders, TableRegister tables, FileStore files) {
        this.orders = orders;
        this.tables = tables;
        this.files = files;
    }

    // ------------------------------ M12 checksum ------------------------------

    /**
     * Recursive weighted digit sum, digits taken right to left with weights 1, 2, 3, 4 ...
     * 2001 -> 1x1 + 0x2 + 0x3 + 2x4 = 9
     * Base case: no digits left (number == 0) -> 0.
     * Recursive case: last digit x weight + weightedSum(remaining digits, weight + 1).
     */
    public static int weightedSum(int number, int weight) {
        if (number == 0) {
            return 0;
        }
        return (number % 10) * weight + weightedSum(number / 10, weight + 1);
    }

    public static int checkDigit(int billNumber) {
        return weightedSum(billNumber, 1) % 10;
    }

    public static String makeBillId(int billNumber) {
        return billNumber + "-" + checkDigit(billNumber);
    }

    /** Shows the working, e.g. "1x1 + 0x2 + 0x3 + 2x4 = 9 -> 9 % 10 = 9" */
    public static String explain(int billNumber) {
        StringBuilder sb = new StringBuilder();
        String digits = String.valueOf(billNumber);
        int weight = 1;
        for (int i = digits.length() - 1; i >= 0; i--) {
            if (weight > 1) {
                sb.append(" + ");
            }
            sb.append(digits.charAt(i)).append('x').append(weight);
            weight++;
        }
        int sum = weightedSum(billNumber, 1);
        sb.append(" = ").append(sum).append(" ").append(Colors.arrow()).append(" ").append(sum)
          .append(" % 10 = ").append(sum % 10);
        return sb.toString();
    }

    /** Splits "2001-9" into {2001, 9} using indexOf() and substring(). Format: NNNN-D. */
    public static int[] parseBillId(String billId) throws InvalidBillFormatException {
        String s = (billId == null) ? "" : billId.trim();
        int dash = s.indexOf('-');
        if (dash != 4 || s.length() != 6) {
            throw new InvalidBillFormatException("use the format NNNN-D, e.g. 2001-9 (you entered " + s + ")");
        }
        try {
            int number = Integer.parseInt(s.substring(0, dash));
            int digit = Integer.parseInt(s.substring(dash + 1));
            if (s.charAt(0) == '-' || s.charAt(0) == '+') {
                throw new NumberFormatException("sign not allowed");
            }
            return new int[]{number, digit};
        } catch (NumberFormatException e) {
            throw new InvalidBillFormatException("bill ID must be digits: NNNN-D, e.g. 2001-9");
        }
    }

    /** Full verification: format -> check digit -> bill exists (binary search). */
    public Bill verify(String billId) throws InvalidBillFormatException, InvalidBillException, BillNotFoundException {
        int[] parts = parseBillId(billId);
        int expected = checkDigit(parts[0]);
        if (parts[1] != expected) {
            throw new InvalidBillException("check digit should be " + expected + " (typing error or swapped digits?)");
        }
        // Arrays.binarySearch works because billNumbers[0..count) is sorted
        int index = Arrays.binarySearch(billNumbers, 0, count, parts[0]);
        if (index < 0) {
            throw new BillNotFoundException("checksum is valid but bill " + billId.trim() + " does not exist");
        }
        return bills[index];
    }

    // ------------------------------ M06 billing ------------------------------

    /**
     * Calculates {subtotal, discount, cgst, sgst, grandTotal} for any Billable.
     * 920 - 92 = 828 taxable; CGST = SGST = 828 x 2.5% = 20.70; total = 869.40
     */
    public static double[] calculate(Billable b) {
        double subtotal = b.getSubtotal();
        double discount = b.getDiscount();
        double taxable = Bill.round2(subtotal - discount);
        double cgst = Bill.round2(taxable * Billable.CGST_RATE);
        double sgst = Bill.round2(taxable * Billable.SGST_RATE);
        double total = Bill.round2(taxable + cgst + sgst);
        return new double[]{subtotal, discount, cgst, sgst, total};
    }

    /** Finds an order that may be billed: it must exist and be SERVED. */
    public Order billableOrder(int orderId) throws OrderNotFoundException, InvalidStatusChangeException {
        Order o = orders.findOrder(orderId);
        if (o.getStatus() == Order.BILLED) {
            throw new InvalidStatusChangeException("order #" + orderId + " is already billed ("
                    + makeBillId(orderId) + ")");
        }
        if (o.getStatus() != Order.SERVED) {
            throw new InvalidStatusChangeException("order #" + orderId + " is " + o.getStatusName()
                    + "; billing is allowed only after SERVED");
        }
        return o;
    }

    public static void checkCash(double total, double received) throws InsufficientPaymentException {
        if (received < total) {
            throw new InsufficientPaymentException(Colors.money(Bill.round2(total - received)) + " due");
        }
    }

    public static void checkSplit(int split) throws InvalidSplitException {
        if (split < 1 || split > 8) {
            throw new InvalidSplitException("split count must be 1-8 (you entered " + split + ")");
        }
    }

    // ---- overloaded pay(...) methods: the parameter types decide which one runs ----

    /** Cash payment. */
    public Bill pay(Staff cashier, int orderId, double cashReceived, int split)
            throws DineSyncException {
        AuthService.requireRole(cashier, Staff.CASHIER);
        Order o = billableOrder(orderId);
        double[] amounts = calculate(o);
        checkCash(amounts[4], cashReceived);
        checkSplit(split);
        return complete(o, amounts, Bill.CASH, cashReceived, split, "", cashier);
    }

    /** UPI payment: reference must be exactly 12 digits. */
    public Bill pay(Staff cashier, int orderId, String upiReference, int split)
            throws DineSyncException {
        AuthService.requireRole(cashier, Staff.CASHIER);
        Order o = billableOrder(orderId);
        String ref = (upiReference == null) ? "" : upiReference.trim();
        if (ref.length() != 12 || !ref.chars().allMatch(Character::isDigit)) {
            throw new InvalidPaymentException("UPI reference must be exactly 12 digits");
        }
        checkSplit(split);
        double[] amounts = calculate(o);
        return complete(o, amounts, Bill.UPI, amounts[4], split, ref, cashier);
    }

    /** Card payment (simulated terminal approval - no card details are collected). */
    public Bill pay(Staff cashier, int orderId, int split) throws DineSyncException {
        AuthService.requireRole(cashier, Staff.CASHIER);
        Order o = billableOrder(orderId);
        checkSplit(split);
        double[] amounts = calculate(o);
        return complete(o, amounts, Bill.CARD, amounts[4], split, "", cashier);
    }

    /** Runs only after every check passed: create bill, mark BILLED, free the table. */
    private Bill complete(Order o, double[] amounts, int mode, double received, int split, String ref,
                          Staff cashier) throws DineSyncException {
        if (count == CAPACITY) {
            throw new InvalidPaymentException("bill storage is full");
        }
        String id = makeBillId(o.getOrderId());
        Bill bill = new Bill(id, o, amounts, mode, received, split, ref, cashier.getStaffId());
        bills[count] = bill;
        billNumbers[count] = o.getOrderId();
        count++;
        o.setStatus(Order.BILLED);
        if (o.isDineIn()) {
            tables.release(o.getTableNo());
        }
        if (files != null) {
            files.appendAudit("BILL " + id + " total " + String.format("%.2f", bill.getGrandTotal()) + " "
                    + bill.getPaymentModeName() + " by " + cashier.getStaffId());
        }
        return bill;
    }

    /** Printable receipt. */
    public static String receipt(Bill b) {
        StringBuilder sb = new StringBuilder();
        sb.append("-------- BILL ").append(b.getBillId()).append(" --------\n");
        for (OrderItem line : b.getOrder().getLines()) {
            sb.append(String.format("%-18s x%-2d %10.2f%n", line.getItem().getName(), line.getQuantity(),
                    line.getLineTotal()));
        }
        sb.append(String.format("%-22s %10.2f%n", "Item total", b.getSubtotal()));
        if (b.getDiscount() > 0) {
            sb.append(String.format("%-22s %10.2f%n", "Loyalty discount (10%)", -b.getDiscount()));
        }
        sb.append(String.format("%-22s %10.2f%n", "Taxable amount", b.getTaxable()));
        sb.append(String.format("%-22s %10.2f%n", "CGST 2.5%", b.getCgst()));
        sb.append(String.format("%-22s %10.2f%n", "SGST 2.5%", b.getSgst()));
        sb.append(String.format("%-22s %10s%n", "GRAND TOTAL", Colors.money(b.getGrandTotal())));
        sb.append("Paid by ").append(b.getPaymentModeName());
        if (b.getPaymentMode() == Bill.CASH) {
            sb.append(String.format(" %s received %s %s change %s", Colors.dot(), Colors.money(b.getAmountReceived()),
                    Colors.dot(), Colors.money(b.getChange())));
        } else if (b.getPaymentMode() == Bill.UPI) {
            sb.append(" ").append(Colors.dot()).append(" ref ").append(b.getReference());
        }
        if (b.getSplitCount() > 1) {
            sb.append(String.format("%nSplit %d ways: %s per person", b.getSplitCount(), Colors.money(b.getPerPerson())));
        }
        return sb.toString();
    }

    public Bill[] getAll() {
        return Arrays.copyOf(bills, count);
    }

    public int getCount() { return count; }

    /** Standalone demo of modules M06 + M12 with the document's numbers. */
    public static void main(String[] args) throws Exception {
        System.out.println(Colors.title("== M12 Bill Checksum - standalone demo =="));
        System.out.println("2001: " + explain(2001) + "  -> bill ID " + makeBillId(2001));
        System.out.println("2010: " + explain(2010) + "  -> bill ID " + makeBillId(2010));
        String[] inputs = {"2001/9", "2010-9", "2001-8"};
        BillingService empty = new BillingService(null, null, null);
        for (String in : inputs) {
            try {
                empty.verify(in);
            } catch (DineSyncException e) {
                System.out.println(Colors.err(in + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        System.out.println(Colors.title("== M06 Billing - Priya Sharma example (Rs.920, loyalty visit) =="));
        MenuService menu = new MenuService();
        menu.loadDefaultMenu();
        Customer priya = new Customer("9123456780", "Priya Sharma", true);
        priya.loadHistory(13);
        priya.checkIn();                                  // visit 14 -> 14 % 7 == 0 -> loyalty
        Order o = new Order(Order.DINE_IN, 6, priya, "W104");
        o.addItem(menu.getByNumber(3), 2);                // Veg Biryani 2 x 250
        o.addItem(menu.getByNumber(4), 1);                // Paneer Tikka 220
        o.addItem(menu.getByNumber(11), 2);               // Masala Chai 2 x 40
        o.addItem(new VegItem(14, "Mirchi Bajji", 1, 120, 10, true), 1);
        double[] a = calculate(o);                        // Order is passed as a Billable
        System.out.printf("Subtotal %.2f  Discount %.2f  CGST %.2f  SGST %.2f  Total %.2f%n",
                a[0], a[1], a[2], a[3], a[4]);
        System.out.printf("Cash 900 -> change %.2f ; split 2 -> %.2f each%n",
                Bill.round2(900 - a[4]), Bill.round2(a[4] / 2));
    }
}
