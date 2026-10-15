
/** LPG concentration. Limit 100-1000 ppm (default 500). */
public class GasLeakSensor extends AbstractSensor {
    public GasLeakSensor() {
        super("LPG Gas", "ppm", 100, 1000, 500, 150, 520);
    }

    @Override
    public String getAutoAction() { return "gas valve CLOSED, alarm ON, exhaust fan ON"; }
}
