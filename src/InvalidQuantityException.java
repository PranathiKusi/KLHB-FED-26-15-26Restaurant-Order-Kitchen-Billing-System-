
/** Quantity outside 1-10 (module M04). */
public class InvalidQuantityException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidQuantityException(String message) {
        super("M04", message);
    }
}
