
/** Dish name, category, type or preparation time is invalid (module M02). */
public class InvalidDishException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidDishException(String message) {
        super("M02", message);
    }
}
