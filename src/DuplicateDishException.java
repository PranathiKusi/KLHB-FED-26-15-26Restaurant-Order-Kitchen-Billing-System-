
/** Dish name already exists (case-insensitive) (module M02). */
public class DuplicateDishException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public DuplicateDishException(String message) {
        super("M02", message);
    }
}
