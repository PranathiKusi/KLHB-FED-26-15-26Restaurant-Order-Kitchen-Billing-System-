import java.util.Scanner;

/**
 * All keyboard input goes through this class (Scanner).
 * Every read method keeps asking until the value is valid, so a typing mistake
 * never crashes the program. When input comes from a file (scripted demo) the typed
 * value is echoed so the transcript is readable.
 */
public class InputHelper {
    private final Scanner sc;
    private final boolean echo;

    public InputHelper(Scanner sc, boolean echo) {
        this.sc = sc;
        this.echo = echo;
    }

    private static String marker() {
        return Colors.isUnicode() ? " ▸ " : " > ";
    }

    /** Reads one line (trimmed). An empty line is allowed. */
    public String readLine(String prompt) {
        System.out.print(prompt + marker());
        String line = sc.nextLine();         // NoSuchElementException at end of input is handled in Main
        if (echo) {
            System.out.println(line);
        }
        return line.trim();
    }

    /** Reads a line that must not be blank. */
    public String readNonBlank(String prompt) {
        String s;
        do {                                  // do-while: the body runs at least once
            s = readLine(prompt);
            if (s.isEmpty()) {
                System.out.println(Colors.err("Input cannot be blank"));
            }
        } while (s.isEmpty());
        return s;
    }

    /** Reads any whole number. Letters are rejected with a message (NumberFormatException is caught). */
    public int readInt(String prompt) {
        while (true) {                        // "infinite" loop that ends with return
            String s = readLine(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println(Colors.err("'" + s + "' is not a whole number"));
            }
        }
    }

    /** Overloaded readInt: also checks the range (used for every menu choice). */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            int value = readInt(prompt + " [" + min + "-" + max + "]");
            try {
                if (value < min || value > max) {
                    throw new InvalidMenuChoiceException(value, min, max);   // unchecked exception
                }
                return value;
            } catch (InvalidMenuChoiceException e) {
                System.out.println(Colors.err("InvalidMenuChoiceException: " + e.getMessage()));
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt);
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println(Colors.err("'" + s + "' is not a number"));
            }
        }
    }

    /** Returns 'y' or 'n' (char type). */
    public char readYesNo(String prompt) {
        while (true) {
            String s = readLine(prompt + " (y/n)").toLowerCase();
            if (s.length() == 1 && (s.charAt(0) == 'y' || s.charAt(0) == 'n')) {
                return s.charAt(0);
            }
            System.out.println(Colors.err("Please type y or n"));
        }
    }
}
