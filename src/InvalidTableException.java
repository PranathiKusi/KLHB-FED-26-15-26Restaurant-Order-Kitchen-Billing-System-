
/** Table number outside 1-8, or releasing a table that is already free (module M09). */
public class InvalidTableException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidTableException(String message) {
        super("M09", message);
    }
}
