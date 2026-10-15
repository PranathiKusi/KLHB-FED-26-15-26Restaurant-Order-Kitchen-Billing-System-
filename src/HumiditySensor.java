
/** Relative humidity in %. Limit range 40-90 % (default 80) - range assumed, not given in the spec. */
public class HumiditySensor extends AbstractSensor {
    public HumiditySensor() {
        super("Humidity", "%", 40, 90, 80, 45, 82);
    }

    @Override
    public String getAutoAction() { return "ventilation ON"; }
}
