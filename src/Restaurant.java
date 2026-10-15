/**
 * Object composition: one Restaurant object HAS-A every service and monitor, wired together once.
 * Main, the tests and the demo all use this class, so every part shares the SAME data
 * (one Order[] array, one Bill[] array, one TableRegister ...).
 */
public class Restaurant {
    public final AuthService auth;
    public final MenuService menu;
    public final CustomerService customers;
    public final TableRegister tables;
    public final Kitchen kitchen;
    public final OrderService orders;
    public final BillingService billing;
    public final FeedbackService feedback;
    public final KitchenMonitor monitor;
    public final EnergyMeter energy;
    public final ReportService reports;
    public final FileStore files;

    /** outputFolder = where audit.log and exported reports are written. */
    public Restaurant(String outputFolder) {
        files = new FileStore(outputFolder);
        auth = new AuthService();
        menu = new MenuService();
        customers = new CustomerService();
        tables = new TableRegister();
        kitchen = new Kitchen(auth);
        orders = new OrderService(menu, tables, kitchen);
        billing = new BillingService(orders, tables, files);
        feedback = new FeedbackService(billing);
        monitor = new KitchenMonitor();
        energy = new EnergyMeter();
        reports = new ReportService(orders, billing, feedback, monitor, energy, auth, menu);
    }

    /** Uses the built-in 13-dish menu, or an optional data/menu.csv file if one exists. */
    public String loadMenu() {
        java.nio.file.Path csv = FileStore.findDataFile("menu.csv");
        if (!java.nio.file.Files.exists(csv)) {
            menu.loadDefaultMenu();
            return "built-in default menu (" + menu.getCount() + " dishes)";
        }
        int loaded = files.loadMenuCsv(csv, menu);
        if (loaded == 0) {
            menu.loadDefaultMenu();
            return "built-in default menu (" + menu.getCount() + " dishes)";
        }
        return "menu.csv (" + loaded + " dishes)";
    }
}
