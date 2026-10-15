
/**
 * A restaurant staff member (module M01).
 * Encapsulation: all fields are private; other classes use the public methods.
 */
public class Staff {
    // Role constants (static final = constant shared by all objects)
    public static final int WAITER = 1;
    public static final int CHEF = 2;
    public static final int CASHIER = 3;
    public static final int ADMIN = 4;
    public static final int MAX_ATTEMPTS = 3;

    // index 0 is unused so that ROLE_PREFIX[WAITER] == 'W'
    private static final char[] ROLE_PREFIX = {' ', 'W', 'C', 'K', 'A'};
    private static final String[] ROLE_NAMES = {"", "Waiter", "Chef", "Cashier", "Admin"};

    private final String staffId;
    private final String name;
    private final int role;
    private final String pin;          // kept as String so a PIN like 0427 keeps its leading zero
    private int failedAttempts;
    private boolean locked;

    public Staff(String staffId, String name, int role, String pin) {
        this.staffId = staffId;   // "this" separates the field from the parameter with the same name
        this.name = name;
        this.role = role;
        this.pin = pin;
    }

    public static char prefixFor(int role) { return ROLE_PREFIX[role]; }

    public static String roleName(int role) {
        return (role >= WAITER && role <= ADMIN) ? ROLE_NAMES[role] : "Unknown";
    }

    public boolean pinMatches(String enteredPin) {
        // equals() compares the characters (value). == would compare object references.
        return pin.equals(enteredPin);
    }

    /** Records one wrong PIN and returns how many attempts are left. */
    public int recordFailure() {
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) {
            locked = true;
        }
        return MAX_ATTEMPTS - failedAttempts;
    }

    public void resetAttempts() { failedAttempts = 0; }

    public void unlock() {
        locked = false;
        failedAttempts = 0;
    }

    public String getStaffId() { return staffId; }
    public String getName() { return name; }
    public int getRole() { return role; }
    public String getRoleName() { return roleName(role); }
    public boolean isLocked() { return locked; }
    public int getFailedAttempts() { return failedAttempts; }

    @Override
    public String toString() {
        return getRoleName() + " " + name + " (" + staffId + ")";
    }
}
