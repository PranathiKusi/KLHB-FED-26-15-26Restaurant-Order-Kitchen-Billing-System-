
/** Mobile number is not 10 digits or does not start with 6-9 (module M03). */
public class InvalidPhoneNumberException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidPhoneNumberException(String message) {
        super("M03", message);
    }
}
