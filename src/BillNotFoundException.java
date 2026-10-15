
/** Check digit is valid but no such bill exists (module M12). */
public class BillNotFoundException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public BillNotFoundException(String message) {
        super("M12", message);
    }
}
