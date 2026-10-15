import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * DineSync entry point.
 *
 *   Compile (inside the src folder):   javac *.java
 *   Run:                               java Main
 *   Run with demo data:                java Main --demo
 *
 * Options: --demo   load labelled demo fixtures (Priya Sharma, busy tables, appliances)
 *          --fast   sensor scans do not wait between scans (for quick demos)
 *          --ascii / --unicode     choose symbols (Rs. or the rupee sign)
 *          --color / --no-color    turn colours on or off
 */
public class Main {
    public static void main(String[] args) {
        boolean demo = false;
        boolean fast = false;
        for (String a : args) {
            switch (a.toLowerCase()) {
                case "--demo":     demo = true; break;
                case "--fast":     fast = true; break;
                case "--ascii":    Colors.setUnicode(false); break;
                case "--unicode":  Colors.setUnicode(true); break;
                case "--color":    Colors.setColor(true); break;
                case "--no-color": Colors.setColor(false); break;
                default: System.out.println("Unknown option ignored: " + a);
            }
        }
        Restaurant restaurant = new Restaurant("dinesync-output");
        String source = restaurant.loadMenu();
        System.out.println("Menu loaded from " + source);
        for (String note : restaurant.files.getLoadMessages()) {
            System.out.println("  note: " + note);
        }
        restaurant.monitor.setFastMode(fast);
        if (demo) {
            System.out.println(DemoData.load(restaurant));
        }
        // System.console() is null when input is redirected from a file -> echo the typed values
        boolean echo = System.console() == null;
        Scanner sc = new Scanner(System.in);
        try {
            new ConsoleUI(restaurant, new InputHelper(sc, echo)).run();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input ended - DineSync closed.");
        } finally {
            sc.close();
        }
    }
}
