
/** Split count outside 1-8 (module M06). */
public class InvalidSplitException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidSplitException(String message) {
        super("M06", message);
    }
}
