
/** 100 customers already stored (module M03). */
public class CustomerLimitException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public CustomerLimitException(String message) {
        super("M03", message);
    }
}
