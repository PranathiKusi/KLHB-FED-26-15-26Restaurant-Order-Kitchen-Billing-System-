
/**
 * A completed, paid bill (modules M06 / M12). Objects are created only after a
 * successful payment, so every Bill in the system is a real, paid transaction.
 */
public class Bill implements Billable {
    public static final int CASH = 1;
    public static final int UPI = 2;
    public static final int CARD = 3;
    private static final String[] MODE_NAMES = {"", "Cash", "UPI", "Card"};

    private final String billId;          // e.g. "2001-9"
    private final Order order;
    private final double subtotal;
    private final double discount;
    private final double cgst;
    private final double sgst;
    private final double grandTotal;
    private final int paymentMode;
    private final double amountReceived;
    private final double change;
    private final int splitCount;
    private final double perPerson;
    private final String reference;       // UPI reference or "" for cash/card
    private final String cashierId;

    public Bill(String billId, Order order, double[] amounts, int paymentMode, double amountReceived,
                int splitCount, String reference, String cashierId) {
        this.billId = billId;
        this.order = order;
        this.subtotal = amounts[0];
        this.discount = amounts[1];
        this.cgst = amounts[2];
        this.sgst = amounts[3];
        this.grandTotal = amounts[4];
        this.paymentMode = paymentMode;
        this.amountReceived = amountReceived;
        this.change = round2(amountReceived - grandTotal);
        this.splitCount = splitCount;
        this.perPerson = round2(grandTotal / splitCount);
        this.reference = reference;
        this.cashierId = cashierId;
    }

    /** Rounds money to 2 decimal places (paise). Example: 20.7000001 -> 20.70 */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;      // Math.round returns long; dividing by 100.0 gives double
    }

    public static String modeName(int mode) {
        return (mode >= CASH && mode <= CARD) ? MODE_NAMES[mode] : "?";
    }

    @Override
    public double getSubtotal() { return subtotal; }

    @Override
    public double getDiscount() { return discount; }

    public String getBillId() { return billId; }
    public int getBillNumber() { return order.getOrderId(); }
    public Order getOrder() { return order; }
    public double getCgst() { return cgst; }
    public double getSgst() { return sgst; }
    public double getTaxable() { return round2(subtotal - discount); }
    public double getGrandTotal() { return grandTotal; }
    public int getPaymentMode() { return paymentMode; }
    public String getPaymentModeName() { return modeName(paymentMode); }
    public double getAmountReceived() { return amountReceived; }
    public double getChange() { return change; }
    public int getSplitCount() { return splitCount; }
    public double getPerPerson() { return perPerson; }
    public String getReference() { return reference; }
    public String getCashierId() { return cashierId; }
    public String getDate() { return order.getDate(); }
    public int getHour() { return order.getHour(); }

    @Override
    public String toString() {
        return "Bill " + billId + " (" + getPaymentModeName() + ", total " + String.format("%.2f", grandTotal) + ")";
    }
}
