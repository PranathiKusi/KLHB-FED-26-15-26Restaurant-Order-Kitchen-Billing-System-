
/** Table has fewer seats than the party size (module M09). */
public class TableCapacityException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public TableCapacityException(String message) {
        super("M09", message);
    }
}
