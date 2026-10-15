
/** Name given is not a chef in Staff[] (module M05). */
public class InvalidChefException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidChefException(String message) {
        super("M05", message);
    }
}
