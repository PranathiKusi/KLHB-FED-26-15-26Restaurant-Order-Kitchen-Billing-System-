
/** Sensor limit or scan setting outside the allowed range (module M10). */
public class InvalidThresholdException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidThresholdException(String message) {
        super("M10", message);
    }
}
