/**
 * Module M01 - Login and Access Control.
 * Staff records are kept in a Staff[] array and found with a linear search.
 * Run this file alone:  javac *.java   then   java AuthService
 */
public class AuthService {
    public static final int CAPACITY = 20;

    private final Staff[] staff = new Staff[CAPACITY];
    private int count;

    /** Creates the service with the restaurant's demo staff accounts (demo PINs, not real credentials). */
    public AuthService() {
        addStaff(new Staff("W104", "Kiran", Staff.WAITER, "4821"));
        addStaff(new Staff("W105", "Ravi", Staff.WAITER, "1357"));
        addStaff(new Staff("C201", "Meena", Staff.CHEF, "2468"));
        addStaff(new Staff("C202", "Arjun", Staff.CHEF, "8642"));
        addStaff(new Staff("K301", "Lakshmi", Staff.CASHIER, "3690"));
        addStaff(new Staff("A401", "Suresh", Staff.ADMIN, "9999"));
    }

    private void addStaff(Staff s) {
        if (count < CAPACITY) {
            staff[count++] = s;
        }
    }

    /**
     * Checks the Staff ID format: 4 characters, first letter matches the role, last 3 are digits.
     * Uses charAt(), Character.toUpperCase() and Character.isDigit().
     */
    public static void validateStaffId(int role, String id) throws InvalidStaffIdException {
        if (role < Staff.WAITER || role > Staff.ADMIN) {
            throw new InvalidMenuChoiceException(role, 1, 4);      // unchecked exception
        }
        if (id == null || id.trim().length() != 4) {
            throw new InvalidStaffIdException("Staff ID must be 4 characters, e.g. "
                    + Staff.prefixFor(role) + "104");
        }
        String clean = id.trim();
        char first = Character.toUpperCase(clean.charAt(0));
        if (first != Staff.prefixFor(role)) {
            throw new InvalidStaffIdException(Staff.roleName(role) + " IDs start with '"
                    + Staff.prefixFor(role) + "'");
        }
        for (int i = 1; i < 4; i++) {
            if (!Character.isDigit(clean.charAt(i))) {
                throw new InvalidStaffIdException("characters 2-4 of a Staff ID must be digits");
            }
        }
    }

    /** Linear search through Staff[] (case-insensitive ID). Returns null when not found. */
    public Staff findById(String id) {
        for (int i = 0; i < count; i++) {
            if (staff[i].getStaffId().equalsIgnoreCase(id.trim())) {
                return staff[i];
            }
        }
        return null;
    }

    /**
     * Logs a staff member in. Wrong PINs are counted per account; the third wrong PIN locks it.
     */
    public Staff login(int role, String id, String pin)
            throws InvalidStaffIdException, InvalidPinException, AccountLockedException {
        validateStaffId(role, id);
        Staff s = findById(id);
        if (s == null || s.getRole() != role) {
            throw new InvalidStaffIdException("no " + Staff.roleName(role) + " with ID " + id.trim().toUpperCase());
        }
        if (s.isLocked()) {
            throw new AccountLockedException(s.getStaffId() + " is locked after "
                    + Staff.MAX_ATTEMPTS + " wrong PINs - ask the Admin to unlock it");
        }
        String p = (pin == null) ? "" : pin.trim();
        boolean wellFormed = p.length() == 4 && p.chars().allMatch(Character::isDigit);
        if (!wellFormed || !s.pinMatches(p)) {
            int left = s.recordFailure();
            if (left <= 0) {
                throw new AccountLockedException("3 wrong attempts - " + s.getStaffId() + " is now locked");
            }
            String reason = wellFormed ? "Wrong PIN" : "PIN must be exactly 4 digits";
            throw new InvalidPinException(reason + " (" + left + (left == 1 ? " attempt" : " attempts") + " left)");
        }
        s.resetAttempts();
        return s;
    }

    /** Only an Admin may unlock a locked account. */
    public void unlock(Staff actor, String id) throws UnauthorizedAccessException, InvalidStaffIdException {
        requireRole(actor, Staff.ADMIN);
        Staff s = findById(id);
        if (s == null) {
            throw new InvalidStaffIdException("no staff with ID " + id);
        }
        s.unlock();
    }

    // ---- method overloading: same name, different parameter lists ----
    public static void requireRole(Staff actor, int role) throws UnauthorizedAccessException {
        if (actor == null || actor.getRole() != role) {
            throw new UnauthorizedAccessException(describe(actor) + " cannot do this; only "
                    + Staff.roleName(role) + " can");
        }
    }

    public static void requireRole(Staff actor, int roleA, int roleB) throws UnauthorizedAccessException {
        if (actor == null || (actor.getRole() != roleA && actor.getRole() != roleB)) {
            throw new UnauthorizedAccessException(describe(actor) + " cannot do this; only "
                    + Staff.roleName(roleA) + " or " + Staff.roleName(roleB) + " can");
        }
    }

    private static String describe(Staff actor) {
        return actor == null ? "Nobody logged in" : actor.getRoleName() + " " + actor.getName();
    }

    /** Finds a chef by name (case-insensitive) for chef assignment in M05. */
    public Staff findChefByName(String name) throws InvalidChefException {
        String n = (name == null) ? "" : name.trim();
        for (int i = 0; i < count; i++) {
            if (staff[i].getRole() == Staff.CHEF && staff[i].getName().equalsIgnoreCase(n)) {
                return staff[i];
            }
        }
        throw new InvalidChefException("'" + n + "' is not a chef");
    }

    public Staff[] getAllStaff() {
        Staff[] copy = new Staff[count];
        System.arraycopy(staff, 0, copy, 0, count);
        return copy;
    }

    /** Standalone demo of module M01 (no keyboard input needed). */
    public static void main(String[] args) {
        AuthService auth = new AuthService();
        System.out.println(Colors.title("== M01 Login & Access Control - standalone demo =="));
        String[][] tries = {{"1", "C104", "4821"}, {"1", "W104", "1111"}, {"1", "W104", "4821"},
                            {"2", "C201", "0000"}, {"2", "C201", "0001"}, {"2", "C201", "0002"},
                            {"2", "C201", "2468"}};
        for (String[] t : tries) {
            int role = Integer.parseInt(t[0]);
            try {
                Staff s = auth.login(role, t[1], t[2]);
                System.out.println(Colors.ok("Welcome, " + s));
            } catch (InvalidStaffIdException | InvalidPinException | AccountLockedException e) {
                // multi-catch: one catch block for several exception types
                System.out.println(Colors.err(e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
    }
}
