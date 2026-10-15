
/** Customer name has digits/symbols or is not 2-30 characters (module M03). */
public class InvalidNameException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidNameException(String message) {
        super("M03", message);
    }
}
