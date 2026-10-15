
/**
 * Small helper for coloured, symbol-rich console output.
 * ANSI colour codes are just special Strings understood by most terminals
 * (VS Code, Windows Terminal, Linux/macOS terminals).
 * Run with --no-color or --ascii if your console shows strange characters.
 */
public class Colors {
    // static = one shared copy for the whole program (not one per object)
    // Detected automatically (see detect()); can be forced with --color / --no-color / --ascii / --unicode
    private static boolean colorOn = detectColor();
    private static boolean unicodeOn = detectUnicode();

    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String BOLD = "\u001B[1m";

    /**
     * Colours are on for Linux/macOS terminals, VS Code and Windows Terminal.
     * The old Windows cmd window prints the codes as junk, so colours stay off there.
     */
    private static boolean detectColor() {
        String os = System.getProperty("os.name", "").toLowerCase();
        boolean windows = os.contains("win");
        boolean modernWindowsConsole = System.getenv("WT_SESSION") != null
                || "vscode".equals(System.getenv("TERM_PROGRAM"));
        return !windows || modernWindowsConsole;
    }

    /** Symbols like the rupee sign need a UTF-8 console; otherwise ASCII stand-ins are used. */
    private static boolean detectUnicode() {
        String enc = System.getProperty("stdout.encoding", System.getProperty("sun.stdout.encoding", ""));
        return enc.toUpperCase().contains("UTF");
    }

    public static void setColor(boolean on) { colorOn = on; }
    public static void setUnicode(boolean on) { unicodeOn = on; }
    public static boolean isUnicode() { return unicodeOn; }

    // ternary operator: condition ? valueIfTrue : valueIfFalse
    public static String tick()  { return unicodeOn ? "\u2714" : "[OK]"; }
    public static String cross() { return unicodeOn ? "\u2718" : "[X]"; }
    public static String rupee() { return unicodeOn ? "\u20B9" : "Rs."; }
    public static String star()  { return unicodeOn ? "\u2605" : "*"; }
    public static String bell()  { return unicodeOn ? "\uD83D\uDD14" : "(BUZZ)"; }
    public static String arrow() { return unicodeOn ? "\u2192" : "->"; }
    public static String warnSign() { return unicodeOn ? "\u26A0" : "!"; }
    public static String dot()   { return unicodeOn ? "\u00B7" : "|"; }

    /** Formats money the same way everywhere: Rs.869.40 / \u20B9869.40 */
    public static String money(double amount) {
        return rupee() + String.format("%.2f", amount);
    }

    public static String paint(String colour, String text) {
        return colorOn ? colour + text + RESET : text;
    }

    public static String ok(String text)    { return paint(GREEN, tick() + " " + text); }
    public static String err(String text)   { return paint(RED, cross() + " " + text); }
    public static String warn(String text)  { return paint(YELLOW, warnSign() + " " + text); }
    public static String title(String text) { return paint(BOLD + CYAN, text); }
}
