
/** Feedback already given for this bill (module M07). */
public class DuplicateFeedbackException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public DuplicateFeedbackException(String message) {
        super("M07", message);
    }
}
