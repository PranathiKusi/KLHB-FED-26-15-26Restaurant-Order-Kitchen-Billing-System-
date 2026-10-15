
/**
 * Anything that can be billed must be able to report its subtotal and discount.
 * Order implements it (amounts are calculated) and Bill implements it (amounts are stored).
 * BillingService works with the interface type, not the class ("programming to interfaces").
 * Fields declared in an interface are automatically public static final constants.
 */
public interface Billable {
    double CGST_RATE = 0.025;       // 2.5 %
    double SGST_RATE = 0.025;       // 2.5 %
    double LOYALTY_RATE = 0.10;     // 10 % on every 7th visit

    double getSubtotal();

    double getDiscount();
}
