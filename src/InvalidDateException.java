
/** Date is badly formatted, impossible or in the future (module M08). */
public class InvalidDateException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidDateException(String message) {
        super("M08", message);
    }
}
