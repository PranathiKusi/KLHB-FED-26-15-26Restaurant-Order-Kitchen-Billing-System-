
/** Payment mode invalid or UPI reference not exactly 12 digits (module M06). */
public class InvalidPaymentException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPaymentException(String message) {
        super("M06", message);
    }
}
