
/**
 * Thrown by a sensor when its reading crosses the safety limit (module M10).
 * It carries extra information (reading, limit, automatic action) in its own fields,
 * so the catch block in KitchenMonitor can log a complete alert.
 */
public class SensorAlertException extends DineSyncException {
    private static final long serialVersionUID = 1L;

    private final String sensorName;
    private final double reading;
    private final double limit;
    private final String action;

    public SensorAlertException(String sensorName, double reading, double limit, String action) {
        super("M10", sensorName + " reading " + String.format("%.1f", reading)
                + " crossed limit " + String.format("%.1f", limit) + " -> " + action);
        this.sensorName = sensorName;
        this.reading = reading;
        this.limit = limit;
        this.action = action;
    }

    public String getSensorName() { return sensorName; }
    public double getReading() { return reading; }
    public double getLimit() { return limit; }
    public String getAction() { return action; }
}
