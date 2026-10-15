
/** Raised after 3 wrong PIN attempts; the account stays locked until an Admin unlocks it (module M01). */
public class AccountLockedException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) {
        super("M01", message);
    }
}
