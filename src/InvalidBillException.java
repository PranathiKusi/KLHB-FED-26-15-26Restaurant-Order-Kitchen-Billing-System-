
/** Bill ID fails the check-digit test (module M07/M12). */
public class InvalidBillException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidBillException(String message) {
        super("M12", message);
    }
}
