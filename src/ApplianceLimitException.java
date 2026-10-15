
/** More than 15 appliances (module M13). */
public class ApplianceLimitException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public ApplianceLimitException(String message) {
        super("M13", message);
    }
}
