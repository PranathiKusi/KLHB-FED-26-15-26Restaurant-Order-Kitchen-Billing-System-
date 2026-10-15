
/**
 * A kitchen appliance for the energy meter (module M13).
 * Power (kW) = V x I x PF / 1000 ;  Energy (kWh) = Power x hours
 */
public class Appliance {
    private final String name;
    private final double voltage;
    private final double current;
    private final double powerFactor;
    private final double hours;

    public Appliance(String name, double voltage, double current, double powerFactor, double hours) {
        this.name = name;
        this.voltage = voltage;
        this.current = current;
        this.powerFactor = powerFactor;
        this.hours = hours;
    }

    public double getPowerKw() {
        return voltage * current * powerFactor / 1000.0;
    }

    public double getEnergyKwh() {
        return getPowerKw() * hours;
    }

    public String getName() { return name; }
    public double getVoltage() { return voltage; }
    public double getCurrent() { return current; }
    public double getPowerFactor() { return powerFactor; }
    public double getHours() { return hours; }

    @Override
    public String toString() {
        return String.format("%-12s %6.1f V %5.2f A PF %.2f %5.1f h -> %.2f kW, %.2f kWh",
                name, voltage, current, powerFactor, hours, getPowerKw(), getEnergyKwh());
    }
}
