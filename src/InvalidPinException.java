
/** PIN does not match or is not exactly 4 digits (module M01). */
public class InvalidPinException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPinException(String message) {
        super("M01", message);
    }
}
