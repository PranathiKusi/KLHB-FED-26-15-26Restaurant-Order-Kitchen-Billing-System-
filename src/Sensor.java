

/**
 * Contract for every simulated kitchen sensor (module M10).
 * KitchenMonitor stores all sensors in one Sensor[] array and calls these methods
 * without knowing the exact class: runtime polymorphism.
 */
public interface Sensor {
    String getName();

    String getUnit();

    /** Takes one (simulated) reading. */
    double read();

    /** Throws SensorAlertException when the reading is above the limit. */
    void check(double reading) throws SensorAlertException;

    double getLimit();

    void setLimit(double limit) throws InvalidThresholdException;

    /** The automatic action the kitchen takes when this sensor raises an alert. */
    String getAutoAction();
}
