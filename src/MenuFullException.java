
/** Menu already holds 30 dishes (module M02). */
public class MenuFullException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public MenuFullException(String message) {
        super("M02", message);
    }
}
