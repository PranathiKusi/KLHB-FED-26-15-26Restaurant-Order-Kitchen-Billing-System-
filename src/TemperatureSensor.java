
/** Kitchen air temperature near the burners. Limit 30-80 degrees C (default 55). */
public class TemperatureSensor extends AbstractSensor {
    public TemperatureSensor() {
        super("Temperature", Colors.isUnicode() ? "\u00B0C" : "C", 30, 80, 55, 36, 58);
    }

    @Override
    public String getAutoAction() { return "exhaust fan ON, burner reduced"; }
}
