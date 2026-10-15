

/**
 * Stores the status of 8 tables in ONE 8-bit register (module M09), like a hardware status register.
 * Bit layout (left = most significant):  T1 T2 T3 T4 T5 T6 T7 T8
 * Table t uses mask (1 << (8 - t)). A 1 bit means occupied.
 * Example: 0100 1010 (0x4A) means T2, T5 and T7 are busy.
 */
public class TableRegister {
    public static final int TABLES = 8;
    private static final int[] SEATS = {2, 2, 4, 4, 4, 6, 6, 8};   // SEATS[t - 1]

    private int reg;     // only the lowest 8 bits are used

    public static int mask(int table) {
        return 1 << (TABLES - table);          // left-shift operator
    }

    public static int seatsOf(int table) { return SEATS[table - 1]; }

    private static void checkTableNumber(int table) throws InvalidTableException {
        if (table < 1 || table > TABLES) {
            throw new InvalidTableException("table must be 1-" + TABLES + " (you entered " + table + ")");
        }
    }

    public static void checkPartySize(int party) throws InvalidInputException {
        if (party < 1 || party > 8) {
            throw new InvalidInputException("party size must be 1-8 guests");
        }
    }

    public boolean isOccupied(int table) {
        return (reg & mask(table)) != 0;       // bitwise AND tests one bit
    }

    /** Smallest free table with seats >= party (lowest number on a tie). Returns 0 if none fits. */
    public int suggest(int party) throws InvalidInputException {
        checkPartySize(party);
        int best = 0;
        for (int t = 1; t <= TABLES; t++) {
            if (isOccupied(t) || SEATS[t - 1] < party) {
                continue;                      // skip busy or too-small tables
            }
            if (best == 0 || SEATS[t - 1] < SEATS[best - 1]) {
                best = t;
            }
        }
        return best;
    }

    public void allocate(int table, int party)
            throws InvalidTableException, TableCapacityException, TableOccupiedException, InvalidInputException {
        checkPartySize(party);
        checkTableNumber(table);
        if (isOccupied(table)) {                 // busy is checked first (matches the document's sample run)
            throw new TableOccupiedException("T" + table + " is busy");
        }
        if (SEATS[table - 1] < party) {
            throw new TableCapacityException("T" + table + " has " + SEATS[table - 1] + " seats; party is " + party);
        }
        reg |= mask(table);                    // bitwise OR sets the bit to 1
    }

    public void release(int table) throws InvalidTableException {
        checkTableNumber(table);
        if (!isOccupied(table)) {
            throw new InvalidTableException("T" + table + " is already free");
        }
        reg &= ~mask(table);                   // AND with NOT-mask clears the bit to 0
    }

    public int getValue() { return reg; }

    public int occupiedCount() {
        int count = 0;
        for (int t = 1; t <= TABLES; t++) {
            if (isOccupied(t)) {
                count++;
            }
        }
        return count;
    }

    /** "0100 1010" - 8 bits split into two nibbles. */
    public String toBinary() {
        String bits = String.format("%8s", Integer.toBinaryString(reg & 0xFF)).replace(' ', '0');
        return bits.substring(0, 4) + " " + bits.substring(4);
    }

    public String toHex() {
        return String.format("0x%02X", reg & 0xFF);
    }

    public String describe() {
        return toBinary() + " = " + toHex() + " (" + reg + ")";
    }

    /**
     * Floor plan drawn with nested loops: 2 rows x 4 tables.
     *   [T1 2] [T2 2#] ...   (# = occupied)
     */
    public String floorPlan() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int t = row * 4 + col + 1;
                sb.append(String.format("[T%d %d seats %s] ", t, SEATS[t - 1], isOccupied(t) ? "BUSY" : "free"));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Demo fixture / test helper: sets the whole register at once (e.g. 0x4A). */
    public void loadState(int value) {
        reg = value & 0xFF;
    }

    /** Standalone demo of module M09 using the document's example (0x4A -> 0x4E -> 0x4A). */
    public static void main(String[] args) {
        TableRegister r = new TableRegister();
        r.loadState(0x4A);                       // demo fixture: T2, T5, T7 already busy
        System.out.println(Colors.title("== M09 Table Occupancy Register - standalone demo =="));
        try {
            System.out.println("Tables " + r.describe() + " " + Colors.dot() + " Suggested for 5: T" + r.suggest(5));
        } catch (InvalidInputException e) {
            System.out.println(Colors.err(e.getMessage()));
        }
        int[] tries = {3, 5, 9, 6};
        for (int t : tries) {
            try {
                r.allocate(t, 5);
                System.out.println(Colors.ok("reg |= 1 << " + (TABLES - t) + " " + Colors.arrow() + " " + r.describe()));
            } catch (DineSyncException e) {
                System.out.println(Colors.err("T" + t + ": " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        try {
            r.release(6);
            System.out.println(Colors.ok("reg &= ~mask " + Colors.arrow() + " " + r.describe()));
        } catch (InvalidTableException e) {
            System.out.println(Colors.err(e.getMessage()));
        }
    }
}
