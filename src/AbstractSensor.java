

/**
 * Shared code for the four sensors. It is abstract because the auto-action differs
 * per sensor, so each subclass must override getAutoAction().
 * (Abstract class + interface together: the class promises the Sensor contract.)
 */
public abstract class AbstractSensor implements Sensor {
    private final String name;
    private final String unit;
    private final double minLimit;     // allowed range for the admin's threshold
    private final double maxLimit;
    private double limit;
    private final double simLow;       // simulated readings fall between simLow and simHigh
    private final double simHigh;
    private Double forcedNext;         // wrapper type Double can be null = "no forced reading" (autoboxing)

    protected AbstractSensor(String name, String unit, double minLimit, double maxLimit, double defaultLimit,
                             double simLow, double simHigh) {
        this.name = name;
        this.unit = unit;
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
        this.limit = defaultLimit;
        this.simLow = simLow;
        this.simHigh = simHigh;
    }

    @Override
    public double read() {
        double value;
        if (forcedNext != null) {
            value = forcedNext;            // auto-unboxing Double -> double
            forcedNext = null;
        } else {
            value = simLow + Math.random() * (simHigh - simLow);
        }
        return Math.round(value * 10) / 10.0;   // one decimal place
    }

    @Override
    public void check(double reading) throws SensorAlertException {
        if (reading > limit) {
            throw new SensorAlertException(name, reading, limit, getAutoAction());
        }
    }

    @Override
    public void setLimit(double newLimit) throws InvalidThresholdException {
        if (newLimit < minLimit || newLimit > maxLimit) {
            throw new InvalidThresholdException(name + " limit must be " + fmt(minLimit) + "-" + fmt(maxLimit)
                    + " " + unit + " only");
        }
        this.limit = newLimit;
    }

    /** Demo/test hook: the next read() returns this value instead of a random one. */
    public void forceNextReading(double value) {
        forcedNext = value;                // autoboxing double -> Double
    }

    private static String fmt(double v) {
        return (v == (int) v) ? String.valueOf((int) v) : String.valueOf(v);   // 30.0 -> "30"
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getUnit() { return unit; }

    @Override
    public double getLimit() { return limit; }

    public double getMinLimit() { return minLimit; }
    public double getMaxLimit() { return maxLimit; }
}
