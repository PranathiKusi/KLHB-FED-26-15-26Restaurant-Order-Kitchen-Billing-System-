
/** A status change skips or reverses a stage, or billing is attempted before SERVED (module M05). */
public class InvalidStatusChangeException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidStatusChangeException(String message) {
        super("M05", message);
    }
}
