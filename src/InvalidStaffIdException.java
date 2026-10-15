
/** Staff ID format is wrong, the role letter does not match, or the ID does not exist (module M01). */
public class InvalidStaffIdException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public InvalidStaffIdException(String message) {
        super("M01", message);
    }
}
