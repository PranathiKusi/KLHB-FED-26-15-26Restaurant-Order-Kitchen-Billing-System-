
/**
 * Raised when a menu number is outside the allowed range.
 * This one extends RuntimeException, so it is UNCHECKED: the compiler does not force
 * a try/catch. It is used to show the difference between checked and unchecked exceptions.
 */
public class InvalidMenuChoiceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidMenuChoiceException(int choice, int min, int max) {
        super("Choice " + choice + " is not valid; enter " + min + "-" + max);
    }
}
