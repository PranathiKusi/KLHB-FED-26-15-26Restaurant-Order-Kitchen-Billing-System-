
/** Voltage, current, power factor, hours or appliance name outside range (module M13). */
public class InvalidReadingException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidReadingException(String message) {
        super("M13", message);
    }
}
