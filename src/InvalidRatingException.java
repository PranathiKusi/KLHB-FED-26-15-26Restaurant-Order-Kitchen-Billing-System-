
/** Rating outside 1-5 (module M07). */
public class InvalidRatingException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidRatingException(String message) {
        super("M07", message);
    }
}
