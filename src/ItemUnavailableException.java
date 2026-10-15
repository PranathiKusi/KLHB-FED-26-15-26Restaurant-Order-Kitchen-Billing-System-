
/** Dish is unavailable or not allowed by the customer's diet (module M04). */
public class ItemUnavailableException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public ItemUnavailableException(String message) {
        super("M04", message);
    }
}
