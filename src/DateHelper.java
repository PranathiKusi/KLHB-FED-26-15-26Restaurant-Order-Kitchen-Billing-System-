import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reads the computer clock and validates dd-mm-yyyy dates (used by M04 order stamps and M08 reports).
 * java.time is used ONLY to read today's date and the current time; all date validation
 * is done by hand with split(), Integer.parseInt() and if-else logic.
 */
public class DateHelper {
    private static final int[] DAYS_IN_MONTH = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final String[] MONTH_NAMES = {"", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"};

    public static String today() {
        LocalDate d = LocalDate.now();
        return String.format("%02d-%02d-%04d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    public static String nowTime() {
        LocalTime t = LocalTime.now();
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    public static int currentHour() {
        return LocalTime.now().getHour();
    }

    /** Leap year rule: divisible by 4, except centuries, unless divisible by 400. */
    public static boolean isLeap(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static int daysIn(int month, int year) {
        return (month == 2 && isLeap(year)) ? 29 : DAYS_IN_MONTH[month];
    }

    /**
     * Validates "dd-mm-yyyy" and checks it is not after 'today' (also dd-mm-yyyy).
     * Returns {day, month, year}. Letters cause NumberFormatException, which is caught and
     * converted into InvalidDateException.
     */
    public static int[] validate(String text, String today) throws InvalidDateException {
        String s = (text == null) ? "" : text.trim();
        String[] parts = s.split("-");
        if (parts.length != 3 || parts[0].length() != 2 || parts[1].length() != 2 || parts[2].length() != 4) {
            throw new InvalidDateException("use the format dd-mm-yyyy, e.g. 26-09-2026");
        }
        int day;
        int month;
        int year;
        try {
            day = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            year = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new InvalidDateException("date must contain digits only (" + e.getMessage() + ")");
        }
        if (month < 1 || month > 12) {
            throw new InvalidDateException("month must be 01-12");
        }
        if (day < 1 || day > daysIn(month, year)) {
            throw new InvalidDateException(MONTH_NAMES[month] + " " + year + " has " + daysIn(month, year) + " days");
        }
        if (compare(new int[]{day, month, year}, parse(today)) > 0) {
            throw new InvalidDateException(s + " is in the future (today is " + today + ")");
        }
        return new int[]{day, month, year};
    }

    private static int[] parse(String ddmmyyyy) {
        String[] p = ddmmyyyy.split("-");
        return new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])};
    }

    /** Compares two {d, m, y} dates: negative if a is earlier, 0 if same, positive if later. */
    private static int compare(int[] a, int[] b) {
        int keyA = a[2] * 10000 + a[1] * 100 + a[0];       // yyyymmdd as one number
        int keyB = b[2] * 10000 + b[1] * 100 + b[0];
        return Integer.compare(keyA, keyB);
    }
}
