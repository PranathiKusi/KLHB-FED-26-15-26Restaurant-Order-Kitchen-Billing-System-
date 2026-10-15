
/** Bill ID is not in the NNNN-D format (module M12). */
public class InvalidBillFormatException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidBillFormatException(String message) {
        super("M12", message);
    }
}
