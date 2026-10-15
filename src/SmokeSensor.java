
/** Smoke density in %. Limit range 10-50 % (default 30) - range assumed, not given in the spec. */
public class SmokeSensor extends AbstractSensor {
    public SmokeSensor() {
        super("Smoke", "%", 10, 50, 30, 5, 32);
    }

    @Override
    public String getAutoAction() { return "chimney fan HIGH, alarm ON"; }
}
