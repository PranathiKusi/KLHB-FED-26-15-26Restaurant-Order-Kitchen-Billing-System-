
/** A logged-in role tried an operation it is not allowed to perform (module M01). */
public class UnauthorizedAccessException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedAccessException(String message) {
        super("M01", message);
    }
}
