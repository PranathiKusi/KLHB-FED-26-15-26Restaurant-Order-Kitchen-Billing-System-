
/** Order ID is not in the active kitchen queue / order list (module M05). */
public class OrderNotFoundException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public OrderNotFoundException(String message) {
        super("M05", message);
    }
}
