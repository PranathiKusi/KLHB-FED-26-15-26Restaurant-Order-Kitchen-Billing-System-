

import java.util.function.Consumer;

/**
 * Runs simulated kitchen sensor scans (module M10).
 * All four sensors live in ONE Sensor[] array. A reading above its limit throws
 * SensorAlertException, which is caught here, logged in String[] alertLog, and
 * scanning continues - an alert never stops the restaurant program.
 */
public class KitchenMonitor {
    public static final int LOG_CAPACITY = 100;

    private final Sensor[] sensors = {
        new TemperatureSensor(), new GasLeakSensor(), new SmokeSensor(), new HumiditySensor()
    };
    private final String[] alertLog = new String[LOG_CAPACITY];
    private final String[] alertDates = new String[LOG_CAPACITY];
    private int logged;          // entries stored in alertLog
    private int totalAlerts;     // all alerts, even if the log is full
    private boolean fastMode;    // tests and the scripted demo skip the real waiting time

    public void setFastMode(boolean fastMode) { this.fastMode = fastMode; }

    public Sensor[] getSensors() { return sensors.clone(); }

    public Sensor getSensor(int index) { return sensors[index]; }

    /** Validates scan settings: 1-50 scans, 1-10 seconds apart. */
    public static void validateScanSettings(int scans, int intervalSeconds) throws InvalidThresholdException {
        if (scans < 1 || scans > 50) {
            throw new InvalidThresholdException("number of scans must be 1-50");
        }
        if (intervalSeconds < 1 || intervalSeconds > 10) {
            throw new InvalidThresholdException("scan interval must be 1-10 seconds");
        }
    }

    /**
     * Runs several scans. Each output line is handed to "out" (a Consumer, i.e. a function that
     * accepts a String). The console passes System.out::println; tests pass a lambda that stores lines.
     * Returns the number of alerts raised in this session.
     */
    public int runScans(int scans, int intervalSeconds, String date, String time, Consumer<String> out)
            throws InvalidThresholdException {
        validateScanSettings(scans, intervalSeconds);
        int alertsThisRun = 0;
        try {
            for (int scan = 1; scan <= scans; scan++) {
                out.accept("Scan " + scan + " " + Colors.dot() + " " + time);
                alertsThisRun += scanOnce(date, time, out);
                if (scan < scans) {
                    Thread.sleep(fastMode ? 0 : intervalSeconds * 1000L);
                }
            }
        } catch (InterruptedException e) {
            // Built-in checked exception from Thread.sleep: report it and stop scanning safely
            Thread.currentThread().interrupt();
            out.accept(Colors.warn("Scan interrupted - monitoring stopped safely"));
        } finally {
            // finally always runs, whether the loop ended normally or was interrupted
            out.accept("Scan session closed " + Colors.dot() + " alerts this session: " + alertsThisRun
                    + " " + Colors.dot() + " alerts today: " + totalAlerts);
        }
        return alertsThisRun;
    }

    /** One pass over all sensors; used by runScans and as a quick background check. */
    public int scanOnce(String date, String time, Consumer<String> out) {
        int alerts = 0;
        for (Sensor s : sensors) {                     // enhanced for loop over the array
            double reading = s.read();                 // which read()? decided at runtime
            String label = String.format("  %-12s %7.1f %-4s", s.getName(), reading, s.getUnit());
            try {
                s.check(reading);
                out.accept(label + " OK");
            } catch (SensorAlertException e) {
                alerts++;
                logAlert(date, time, e);
                out.accept(Colors.paint(Colors.YELLOW, label + " " + Colors.warnSign() + " ALERT "
                        + Colors.arrow() + " " + e.getAction()));
            }
        }
        return alerts;
    }

    private void logAlert(String date, String time, SensorAlertException e) {
        totalAlerts++;
        if (logged < LOG_CAPACITY) {
            // StringBuffer is the thread-safe mutable string class (StringBuilder is its faster twin)
            StringBuffer entry = new StringBuffer();
            entry.append(date).append(' ').append(time).append(" | ").append(e.getMessage());
            alertLog[logged] = entry.toString();
            alertDates[logged] = date;
            logged++;
        }
    }

    /** Counts alerts logged on a given date (used by M08 reports). */
    public int countAlertsOn(String date) {
        int count = 0;
        for (int i = 0; i < logged; i++) {
            if (alertDates[i].equals(date)) {
                count++;
            }
        }
        return count;
    }

    public String[] getAlertLog() {
        String[] copy = new String[logged];
        System.arraycopy(alertLog, 0, copy, 0, logged);
        return copy;
    }

    public int getTotalAlerts() { return totalAlerts; }

    /** Standalone demo of module M10: threshold check, a forced overheat reading, 3 fast scans. */
    public static void main(String[] args) {
        KitchenMonitor km = new KitchenMonitor();
        km.setFastMode(true);
        System.out.println(Colors.title("== M10 Kitchen Sensor Monitoring - standalone demo =="));
        try {
            km.getSensor(0).setLimit(120);
        } catch (InvalidThresholdException e) {
            System.out.println(Colors.err("InvalidThresholdException: " + e.getMessage()));
        }
        ((AbstractSensor) km.getSensor(0)).forceNextReading(58.4);     // downcast to reach the demo hook
        try {
            km.runScans(3, 2, DateHelper.today(), DateHelper.nowTime(), System.out::println);
        } catch (InvalidThresholdException e) {
            System.out.println(Colors.err(e.getMessage()));
        }
        for (String line : km.getAlertLog()) {
            System.out.println("LOG: " + line);
        }
    }
}
