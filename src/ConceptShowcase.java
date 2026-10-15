import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * SUPPORTING DEMONSTRATION (not part of the restaurant workflow).
 * Shows syllabus topics that are easier to see in isolation - each one uses DineSync data.
 * Open it from the main menu (option 3) or run alone:  java ConceptShowcase
 */
public class ConceptShowcase {

    public static void runAll(Restaurant r) {
        typesAndOperators();
        stringsDemo();
        matrixDemo();
        collectionsDemo(r.menu);
        functionalDemo(r.menu);
        recursionDemo();
    }

    private static void heading(String text) {
        System.out.println();
        System.out.println(Colors.title("[Concept] " + text));
    }

    /** CO1: data types, casting, precedence, associativity, unary, ternary, bitwise operators. */
    static void typesAndOperators() {
        heading("Data types, operators, casting");
        int subtotal = 920;                       // int: whole rupees
        double rate = 0.025;                      // double: fractions
        final int VISITS_FOR_LOYALTY = 7;         // constant
        char spiceCode = 'M';
        boolean vegOnly = true;
        long orderSequence = 2001L;
        System.out.println("int " + subtotal + ", double " + rate + ", char " + spiceCode + ", boolean " + vegOnly
                + ", long " + orderSequence);
        System.out.println("920 / 7 = " + (subtotal / 7) + " (integer division)   920 / 7.0 = "
                + (subtotal / 7.0) + " (int widened to double)");
        double average = 869.40 / 2;
        System.out.println("(int) 434.7 = " + (int) average + " (explicit cast drops the fraction)");
        System.out.println("2 + 3 * 4 = " + (2 + 3 * 4) + " (precedence: * before +)   20 - 6 - 4 = "
                + (20 - 6 - 4) + " (left-to-right associativity)");
        int visits = 13;
        visits++;                                 // unary increment
        visits += 0;                              // compound assignment
        System.out.println("visits++ -> " + visits + "; loyalty? " + (visits % VISITS_FOR_LOYALTY == 0 ? "YES" : "no")
                + " (ternary operator)");
        System.out.println("(subtotal > 500 && vegOnly) = " + (subtotal > 500 && vegOnly)
                + "   !vegOnly = " + !vegOnly);
        int reg = 0x4A;
        System.out.println("Bitwise on table register 0x4A: | (1<<2) = 0x" + Integer.toHexString(reg | (1 << 2))
                + ", & ~(1<<1) = 0x" + Integer.toHexString(reg & ~(1 << 1)) + ", ^ 0xFF = 0x"
                + Integer.toHexString(reg ^ 0xFF) + ", >> 4 = " + (reg >> 4));
        char digit = '7';
        System.out.println("Character conversion: '7' - '0' = " + (digit - '0') + ", (char) ('A' + 2) = "
                + (char) ('A' + 2));
        System.out.printf("Formatted output: %-12s|%8.2f|%05d%n", "Veg Biryani", 250.0, 42);
    }

    /** CO5: immutability, equals vs ==, StringBuilder / StringBuffer, split/join/trim. */
    static void stringsDemo() {
        heading("Strings");
        String a = "2001-9";
        String b = new String("2001-9");
        System.out.println("a.equals(b) = " + a.equals(b) + " (same characters)   a == b = " + (a == b)
                + " (different objects)");
        String name = "  priya sharma ";
        name.toUpperCase();                       // result ignored: Strings are immutable
        System.out.println("After name.toUpperCase() without assignment: '" + name + "' (unchanged)");
        System.out.println("trim + toUpperCase: '" + name.trim().toUpperCase() + "', length " + name.trim().length()
                + ", charAt(0) '" + name.trim().charAt(0) + "', indexOf(\"sharma\") " + name.indexOf("sharma"));
        String record = "Masala Chai,5,3,40,5,y";
        String[] fields = record.split(",");
        System.out.println("split -> " + fields.length + " fields; join -> " + String.join(" | ", fields));
        StringBuilder sb = new StringBuilder("Token");
        sb.append(' ').append(107).insert(0, "[").append("]");
        StringBuffer safe = new StringBuffer("alert");
        safe.reverse();
        System.out.println("StringBuilder: " + sb + "   StringBuffer reversed: " + safe);
        System.out.println("compareToIgnoreCase(\"paneer tikka\", \"Paneer Tikka\") = "
                + "paneer tikka".compareToIgnoreCase("Paneer Tikka"));
    }

    /** CO3: 2-D array matrix arithmetic - lunch + dinner plates sold per category per table type. */
    static void matrixDemo() {
        heading("2-D arrays: matrix addition (sample numbers for illustration)");
        String[] rows = {"2-seat tables", "4-seat tables"};
        String[] cols = {"Starters", "Mains", "Drinks"};
        int[][] lunch = {{4, 6, 5}, {9, 12, 10}};
        int[][] dinner = {{6, 8, 7}, {11, 15, 14}};
        int[][] total = new int[2][3];
        for (int i = 0; i < 2; i++) {             // row-wise
            for (int j = 0; j < 3; j++) {         // column-wise
                total[i][j] = lunch[i][j] + dinner[i][j];
            }
        }
        System.out.printf("%-14s %8s %8s %8s%n", "", cols[0], cols[1], cols[2]);
        for (int i = 0; i < total.length; i++) {
            System.out.printf("%-14s %8d %8d %8d%n", rows[i], total[i][0], total[i][1], total[i][2]);
        }
    }

    /** CO6: ArrayList, HashMap, HashSet, TreeSet, Iterator, autoboxing, generics. */
    static void collectionsDemo(MenuService menu) {
        heading("Collections");
        List<String> names = new ArrayList<>();
        for (MenuItem m : menu.getAll()) {
            names.add(m.getName());
        }
        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            if (it.next().contains("Chicken")) {
                it.remove();                      // safe removal while iterating
            }
        }
        System.out.println("ArrayList without 'Chicken' dishes (" + names.size() + "): " + names);
        Map<String, Double> priceOf = new HashMap<>();
        for (MenuItem m : menu.getAll()) {
            priceOf.put(m.getName(), m.getPrice());   // autoboxing double -> Double
        }
        double chai = priceOf.get("Masala Chai");     // auto-unboxing
        System.out.println("HashMap lookup Masala Chai -> " + chai + ", containsKey(\"Pizza\") = "
                + priceOf.containsKey("Pizza"));
        Set<String> categories = new HashSet<>();
        Set<String> ordered = new TreeSet<>();
        for (MenuItem m : menu.getAll()) {
            categories.add(m.getCategoryName());      // duplicates are ignored by a Set
            ordered.add(m.getCategoryName());
        }
        System.out.println("HashSet size " + categories.size() + "   TreeSet (sorted): " + ordered);
        List<MenuItem> list = new ArrayList<>(Arrays.asList(menu.getAll()));
        java.util.Collections.sort(list);             // uses MenuItem.compareTo (Comparable)
        System.out.println("Collections.sort A-Z first three: " + list.get(0).getName() + ", "
                + list.get(1).getName() + ", " + list.get(2).getName());
    }

    /** CO6: functional interfaces, lambdas, method references, streams, collectors, Optional. */
    static void functionalDemo(MenuService menu) {
        heading("Lambdas and streams");
        Predicate<MenuItem> cheap = m -> m.getPrice() < 100;
        Function<MenuItem, String> label = m -> m.getName() + "@" + (int) m.getPrice();
        Consumer<String> printer = s -> System.out.println("  " + s);
        Supplier<String> today = DateHelper::today;                 // method reference
        Comparator<MenuItem> byPrep = Comparator.comparingInt(MenuItem::getPrepMinutes);
        List<MenuItem> all = Arrays.asList(menu.getAll());
        System.out.println("Cheap dishes (Predicate + Function + Consumer):");
        all.stream().filter(cheap).map(label).forEach(printer);
        System.out.println("Quickest 3 dishes: " + all.stream().sorted(byPrep).limit(3)
                .map(MenuItem::getName).collect(Collectors.toList()));
        System.out.println("Distinct prep times: " + all.stream().map(MenuItem::getPrepMinutes).distinct()
                .sorted().collect(Collectors.toList()));
        System.out.println("Veg dish count: " + all.stream().filter(MenuItem::isVeg).count()
                + "   menu value (reduce): " + all.stream().map(MenuItem::getPrice).reduce(0.0, Double::sum));
        Map<String, Long> perType = all.stream()
                .collect(Collectors.groupingBy(MenuItem::getTypeLabel, Collectors.counting()));
        System.out.println("groupingBy type: " + perType);
        System.out.println("joining: " + all.stream().filter(m -> m.getCategory() == 5).map(MenuItem::getName)
                .collect(Collectors.joining(", ", "[", "]")));
        Optional<MenuItem> pizza = menu.findByName("Pizza");
        System.out.println("Optional for 'Pizza': " + (pizza.isPresent() ? pizza.get() : "empty")
                + "   today (Supplier): " + today.get());
    }

    /** CO3: recursion used by the bill checksum and token padding. */
    static void recursionDemo() {
        heading("Recursion");
        System.out.println("weightedSum(2001, 1) = 1x1 + weightedSum(200, 2) = ... = "
                + BillingService.weightedSum(2001, 1) + " -> check digit " + BillingService.checkDigit(2001));
        System.out.println("padLeft(\"7\", 3) -> padLeft(\"07\", 3) -> padLeft(\"007\", 3) = "
                + SevenSegment.padLeft("7", 3) + " (base case: length reached)");
    }

    public static void main(String[] args) {
        Restaurant r = new Restaurant("dinesync-output");
        r.loadMenu();
        runAll(r);
    }
}
