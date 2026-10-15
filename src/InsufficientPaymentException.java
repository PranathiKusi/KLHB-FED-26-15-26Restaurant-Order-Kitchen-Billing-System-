
/** Cash received is less than the grand total (module M06). */
public class InsufficientPaymentException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InsufficientPaymentException(String message) {
        super("M06", message);
    }
}
