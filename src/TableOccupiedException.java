
/** Bit for that table is already 1 (module M09). */
public class TableOccupiedException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public TableOccupiedException(String message) {
        super("M09", message);
    }
}
