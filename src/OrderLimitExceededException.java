
/** Order would exceed 20 items (module M04). */
public class OrderLimitExceededException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public OrderLimitExceededException(String message) {
        super("M04", message);
    }
}
