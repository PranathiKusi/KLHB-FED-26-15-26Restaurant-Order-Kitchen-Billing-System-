

/**
 * Seven-segment token display (module M11).
 *
 *     _a_
 *   f|   |b      Each digit is a 7-bit code "abcdefg" (bit 6 = a ... bit 0 = g).
 *    |_g_|       Example: 1 = 0110000 (0x30) -> only segments b and c are lit.
 *   e|   |c
 *    |_d_|
 */
public class SevenSegment {
    public static final int ASCII = 1;
    public static final int BINARY = 2;
    public static final int BOTH = 3;

    /** Segment codes from the project document, index = digit. */
    public static final int[] CODES = {0x7E, 0x30, 0x6D, 0x79, 0x33, 0x5B, 0x5F, 0x70, 0x7F, 0x7B};

    /** PATTERNS[digit][row]: 10 digits x 3 rows, built from CODES with bitwise operations. */
    private static final String[][] PATTERNS = buildPatterns();

    private static boolean segmentOn(int code, int segment) {
        // segment 0 = a ... 6 = g ; bit position = 6 - segment
        return ((code >> (6 - segment)) & 1) == 1;
    }

    private static String[][] buildPatterns() {
        String[][] p = new String[10][3];
        for (int d = 0; d < 10; d++) {
            int c = CODES[d];
            p[d][0] = " " + (segmentOn(c, 0) ? "_" : " ") + " ";
            p[d][1] = (segmentOn(c, 5) ? "|" : " ") + (segmentOn(c, 6) ? "_" : " ") + (segmentOn(c, 1) ? "|" : " ");
            p[d][2] = (segmentOn(c, 4) ? "|" : " ") + (segmentOn(c, 3) ? "_" : " ") + (segmentOn(c, 2) ? "|" : " ");
        }
        return p;
    }

    /** Checks 1-3 digits only and pads to 3 digits: "7" -> "007". */
    public static String validateToken(String token) throws InvalidTokenException {
        if (token == null || token.trim().isEmpty()) {
            throw new InvalidTokenException("token is empty");
        }
        String t = token.trim();
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                throw new InvalidTokenException("digits only (found '" + t.charAt(i) + "')");
            }
        }
        if (t.length() > 3) {
            throw new InvalidTokenException("at most 3 digits");
        }
        return padLeft(t, 3);
    }

    /** Recursive helper: adds '0' in front until the string has the wanted length. */
    static String padLeft(String s, int width) {
        if (s.length() >= width) {
            return s;                       // base case
        }
        return padLeft("0" + s, width);     // recursive case: one step closer to the base case
    }

    public static String binaryCode(int digit) {
        return String.format("%7s", Integer.toBinaryString(CODES[digit])).replace(' ', '0');
    }

    public static String hexCode(int digit) {
        return String.format("0x%02X", CODES[digit]);
    }

    /** Builds the display text for a token in the chosen style (1 ASCII, 2 Binary, 3 Both). */
    public static String render(String token, int style) throws InvalidTokenException, InvalidInputException {
        if (style < ASCII || style > BOTH) {
            throw new InvalidInputException("display style must be 1-3");
        }
        String padded = validateToken(token);
        StringBuilder sb = new StringBuilder();
        if (style == ASCII || style == BOTH) {
            for (int row = 0; row < 3; row++) {                 // nested loop: rows outside,
                for (int i = 0; i < padded.length(); i++) {     // digits inside
                    int digit = padded.charAt(i) - '0';         // char -> int conversion
                    sb.append(PATTERNS[digit][row]).append(' ');
                }
                sb.append('\n');
            }
        }
        if (style == BINARY || style == BOTH) {
            for (int i = 0; i < padded.length(); i++) {
                int digit = padded.charAt(i) - '0';
                sb.append(digit).append(" = ").append(binaryCode(digit))
                  .append(" (").append(hexCode(digit)).append(")\n");
            }
        }
        return sb.toString();
    }

    /** Standalone demo of module M11. */
    public static void main(String[] args) {
        System.out.println(Colors.title("== M11 Seven-Segment Token Display - standalone demo =="));
        String[] tokens = {"1A7", "1234", "7", "107"};
        for (String t : tokens) {
            try {
                System.out.println("Token " + t + " -> " + validateToken(t));
                System.out.print(render(t, BOTH));
            } catch (InvalidTokenException | InvalidInputException e) {
                System.out.println(Colors.err("InvalidTokenException: " + e.getMessage()));
            }
        }
    }
}
