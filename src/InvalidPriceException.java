
/** Price outside Rs.10 - Rs.2000 (module M02). */
public class InvalidPriceException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPriceException(String message) {
        super("M02", message);
    }
}
