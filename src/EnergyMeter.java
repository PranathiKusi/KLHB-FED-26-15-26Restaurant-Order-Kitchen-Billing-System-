

/**
 * Kitchen energy meter (module M13).
 * Cost uses a two-slab tariff: first 20 units at Rs.6, remaining units at Rs.8.
 * Total units are rounded to 2 decimals (as a meter displays them) BEFORE the tariff is applied.
 */
public class EnergyMeter {
    public static final int CAPACITY = 15;

    private final Appliance[] appliances = new Appliance[CAPACITY];
    private int count;
    private double slabUnits = 20;
    private double slabRate1 = 6;
    private double slabRate2 = 8;

    public Appliance addAppliance(String name, double volts, double amps, double pf, double hours)
            throws InvalidReadingException, ApplianceLimitException {
        if (count == CAPACITY) {
            throw new ApplianceLimitException("only " + CAPACITY + " appliances can be metered");
        }
        validate(name, volts, amps, pf, hours);
        Appliance a = new Appliance(name.trim(), volts, amps, pf, hours);
        appliances[count++] = a;
        return a;
    }

    // Field-by-field checks (the console re-asks only the field that was wrong)
    public static void checkName(String name) throws InvalidReadingException {
        String n = (name == null) ? "" : name.trim();
        if (n.length() < 2 || n.length() > 20) {
            throw new InvalidReadingException("appliance name must be 2-20 characters");
        }
    }

    public static void checkVoltage(double volts) throws InvalidReadingException {
        if (volts < 100 || volts > 250) {
            throw new InvalidReadingException("voltage must be 100-250 V");
        }
    }

    public static void checkCurrent(double amps) throws InvalidReadingException {
        if (amps < 0.1 || amps > 20) {
            throw new InvalidReadingException("current must be 0.1-20 A");
        }
    }

    public static void checkPowerFactor(double pf) throws InvalidReadingException {
        if (pf <= 0 || pf > 1) {
            throw new InvalidReadingException("0 < PF <= 1");
        }
    }

    public static void checkHours(double hours) throws InvalidReadingException {
        if (hours < 0 || hours > 24) {
            throw new InvalidReadingException("hours per day must be 0-24");
        }
    }

    public static void validate(String name, double volts, double amps, double pf, double hours)
            throws InvalidReadingException {
        checkName(name);
        checkVoltage(volts);
        checkCurrent(amps);
        checkPowerFactor(pf);
        checkHours(hours);
    }

    public void setTariff(double units, double rate1, double rate2) throws InvalidReadingException {
        if (units <= 0 || rate1 <= 0 || rate2 <= 0) {
            throw new InvalidReadingException("slab units and rates must be positive");
        }
        slabUnits = units;
        slabRate1 = rate1;
        slabRate2 = rate2;
    }

    /** Sum of all appliance energies, rounded to 2 decimals (meter reading). */
    public double getTotalUnits() {
        double total = 0;
        for (int i = 0; i < count; i++) {
            total += appliances[i].getEnergyKwh();
        }
        return Bill.round2(total);
    }

    /** cost = min(units, 20) x 6 + max(units - 20, 0) x 8 */
    public double cost(double units) {
        double firstSlab = Math.min(units, slabUnits) * slabRate1;
        double secondSlab = Math.max(units - slabUnits, 0) * slabRate2;
        return Bill.round2(firstSlab + secondSlab);
    }

    public double getTotalCost() {
        return cost(getTotalUnits());
    }

    /** Max-search loop: the appliance with the highest daily energy, or null when empty. */
    public Appliance highest() {
        Appliance best = null;
        for (int i = 0; i < count; i++) {
            if (best == null || appliances[i].getEnergyKwh() > best.getEnergyKwh()) {
                best = appliances[i];
            }
        }
        return best;
    }

    public String report() {
        StringBuilder sb = new StringBuilder();
        Appliance top = highest();
        for (int i = 0; i < count; i++) {
            sb.append(appliances[i]);
            if (appliances[i] == top) {        // == on objects: is it the SAME object?
                sb.append("  <- highest");
            }
            sb.append('\n');
        }
        double units = getTotalUnits();
        double first = Math.min(units, slabUnits);
        double rest = Math.max(units - slabUnits, 0);
        sb.append(String.format("Total %.2f kWh%n", units));
        sb.append(String.format("%.2f x %.2f + %.2f x %.2f = %.2f%n", first, slabRate1, rest, slabRate2, cost(units)));
        return sb.toString();
    }

    public Appliance[] getAppliances() {
        Appliance[] copy = new Appliance[count];
        System.arraycopy(appliances, 0, copy, 0, count);
        return copy;
    }

    public int getCount() { return count; }

    /** Standalone demo of module M13 with the document's four appliances. */
    public static void main(String[] args) {
        EnergyMeter m = new EnergyMeter();
        System.out.println(Colors.title("== M13 Kitchen Energy Meter - standalone demo =="));
        // {volts, amps, power factor, hours}; Fridge and Fan values are assumed inputs that
        // reproduce the document's 5.63 kWh and 1.47 kWh
        String[] names = {"Tandoor", "Induction", "Induction", "Fridge", "Fan"};
        double[][] readings = {{230, 8.5, 1.0, 6}, {230, 9, 1.4, 4}, {230, 9, 0.95, 4}, {230, 1.2, 0.85, 24},
                               {230, 0.4, 0.8, 20}};
        for (int i = 0; i < names.length; i++) {
            double[] r = readings[i];
            try {
                Appliance a = m.addAppliance(names[i], r[0], r[1], r[2], r[3]);
                System.out.println(Colors.ok(String.format("%s %.2f kW %s %.2f kWh", a.getName(), a.getPowerKw(),
                        Colors.arrow(), a.getEnergyKwh())));
            } catch (InvalidReadingException | ApplianceLimitException e) {
                System.out.println(Colors.err(names[i] + ": " + e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        System.out.print(m.report());
    }
}
