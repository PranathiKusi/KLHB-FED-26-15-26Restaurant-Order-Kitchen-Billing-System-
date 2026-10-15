
/** General text/number input that breaks a length or range rule (notes, comments, party size, spice level) (module GEN). */
public class InvalidInputException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super("GEN", message);
    }
}
