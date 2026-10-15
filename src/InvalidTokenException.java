
/** Token has letters or more than 3 digits (module M11). */
public class InvalidTokenException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidTokenException(String message) {
        super("M11", message);
    }
}
