/**
 * DEMO FIXTURES (loaded only with "java Main --demo").
 * These recreate the starting situation of the project document's end-to-end example.
 * They are NOT real transactions and are never counted as bills or revenue.
 */
public class DemoData {
    public static String load(Restaurant r) {
        StringBuilder sb = new StringBuilder();
        sb.append(Colors.warn("DEMO MODE - labelled fixtures loaded:")).append('\n');
        try {
            // 1. Walk-in parties already seated at T2, T5 and T7 -> register 0100 1010 = 0x4A
            r.tables.loadState(0x4A);
            sb.append("  tables T2, T5, T7 busy (walk-ins) -> ").append(r.tables.describe()).append('\n');

            // 2. Returning customer Priya Sharma: 13 earlier visits, veg only, favourite dish counts
            Customer priya = r.customers.register("9123456780", "Priya Sharma", 2);
            priya.loadHistory(13);
            priya.addDish(3, 11);     // Veg Biryani 11x
            priya.addDish(4, 8);      // Paneer Tikka 8x
            priya.addDish(11, 6);     // Masala Chai 6x
            sb.append("  customer Priya Sharma 9123456780, 13 earlier visits (next visit = #14, loyalty)\n");

            // 3. Earlier reviews: 214 reviews totalling 961 stars (average 4.49)
            r.feedback.loadBaseline(214, 961);
            sb.append("  rating history: 214 earlier reviews, average 4.49\n");

            // 4. Three appliances already metered today (Induction is added live in the demo)
            r.energy.addAppliance("Tandoor", 230, 8.5, 1.0, 6);
            r.energy.addAppliance("Fridge", 230, 1.2, 0.85, 24);
            r.energy.addAppliance("Fan", 230, 0.4, 0.8, 20);
            sb.append("  appliances: Tandoor, Fridge, Fan\n");
        } catch (DineSyncException e) {
            sb.append(Colors.err("demo data problem: " + e.getMessage())).append('\n');
        }
        return sb.toString();
    }
}
