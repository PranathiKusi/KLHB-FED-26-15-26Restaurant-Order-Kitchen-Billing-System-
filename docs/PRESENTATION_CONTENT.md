# Presentation Content (18 slides, 16:9)

**Palette:** Navy `#0B1F4B` (backgrounds and titles) · Royal blue `#2856D9` (headings and boxes) · Cyan `#19C3E6` (highlights) · White `#FFFFFF` · Teal accent `#0FA3A3` (success and numbers). Use a monospace font for code and console output.

> Use only real output. Screenshots marked **[SCREENSHOT]** should be taken from your own run (see `DEMO_SCRIPT.md`). Every number below was produced by the program.

---

### 1. Title
**DineSync: Smart Restaurant Management System with IoT-Based Kitchen Monitoring**
Project 15 · Money & Commerce · 26SC1101E PSP (Java) · Pranathi Kusi, M. Sri Akhil Chowdary · Guide: Dr. K. Sreeram Murthy
*Notes:* "Our project connects the waiter, kitchen, cashier and manager of a restaurant in one Java program."

### 2. Problem statement
- Slow floor → kitchen communication in busy hours
- Lost, misread or duplicated orders
- Errors in discount, tax and bill totals
- No record of regular customers
- Kitchen hazards noticed late
- No daily view of sales, dishes or costs
*Notes:* "Each of these is a real problem in small restaurants that still use paper slips."

### 3. Proposed solution
One console application, 13 connected modules, an order typed **once**. Monitoring modules simulate sensors, a token display and an energy meter.
*Notes:* "After the waiter enters the order, nobody types it again. The kitchen, the cashier and the reports all use the same order object."

### 4. Objectives and scope
Objectives: connected flow · accurate bills · customer favourites · validation without crashes · safety alerts · daily reports.
Scope: Core Java prototype, in-memory arrays, console, simulated hardware.
*Notes:* Be clear that this is an academic prototype, not a deployed product.

### 5. Technology stack
Java 17+ (tested on JDK 21) · console + `Scanner` · arrays + Collections · `java.nio.file` for files · VS Code / Notepad + cmd · Git & GitHub · no external libraries.
*Notes:* "Everything is from our syllabus. There is no database or framework."

### 6. 13 modules in 4 clusters
Four columns: A Identity & Tables (M01, M02, M03, M09) · B Orders & Kitchen (M04, M05, M11) · C Billing & Feedback (M06, M12, M07) · D Monitoring & Analytics (M10, M13, M08).
*Notes:* Explain one sentence per cluster.

### 7. Architecture
Diagram: `Main → ConsoleUI → Restaurant → Services → Models / Monitors`, with exceptions and FileStore at the side.
*Notes:* "The UI only asks; the services decide. One `Restaurant` object holds the single copy of all data."

### 8. End-to-end order workflow
Login → Customer (visit 14, loyalty) → Table T6 → Order #2001 → Kitchen → READY token → Served → Bill 2001-9 → T6 released → Feedback → Report.
*Notes:* Walk through Priya Sharma's visit.

### 9. Java concepts and CO mapping
Table CO1–CO6 → example (copy the compact table from `POSTER_CONTENT.md`).
*Notes:* "The full list with class and method names is in `docs/CURRICULUM_MATRIX.md`."

### 10. Actual source-code example
```java
public static int weightedSum(int number, int weight) {
    if (number == 0) {
        return 0;                                   // base case
    }
    return (number % 10) * weight + weightedSum(number / 10, weight + 1);
}
```
*Notes:* "2001 becomes 1×1 + 0×2 + 0×3 + 2×4 = 9, so the bill ID is 2001-9. This is recursion with a base case."

### 11. Order management and kitchen queue
Real output:
```
#2002 T2       URGENT PLACED    ETA 15 min
#2003 T7       URGENT PLACED    ETA 27 min
#2001 T6       Normal PLACED    ETA 24 min
```
Rules: veg-only check, qty 1–10, max 20 items, PLACED → PREPARING → READY → SERVED only.
*Notes:* "Urgent orders first, then older ones; this uses `Comparable` and `Arrays.sort`. ETA is 20 + 2 × 2 = 24 minutes."

### 12. Billing and checksum
Real receipt:
```
Item total                 920.00
Loyalty discount (10%)     -92.00
Taxable amount             828.00
CGST 2.5%                   20.70
SGST 2.5%                   20.70
GRAND TOTAL             Rs.869.40
Paid by Cash | received Rs.900.00 | change Rs.30.60
Split 2 ways: Rs.434.70 per person
```
`2010-9` → "check digit should be 0 (swapped digits?)"
*Notes:* "The bill is created only after the payment is valid. Then the table is freed."

### 13. Table occupancy and bitwise operations
```
Tables 0100 1010 = 0x4A | Suggested: T6
reg |= 1 << 2 -> 0100 1110 = 0x4E (78)
T6 released  -> 0100 1010 = 0x4A (74)
```
*Notes:* "All 8 tables fit in one integer, like a hardware status register. OR sets a bit and AND-NOT clears it."

### 14. Simulated monitoring and energy
Sensor line: `Temperature 58.4 C ! ALERT -> exhaust fan ON, burner reduced` (scanning continues).
Energy: Tandoor 11.73, Induction 7.87, Fridge 5.63, Fan 1.47 → **26.70 kWh → 20 × 6 + 6.70 × 8 = ₹173.60**.
*Notes:* "Four different sensor classes in one `Sensor[]` array: runtime polymorphism. The alert is an exception we catch, so the restaurant keeps running."

### 15. Testing and results
**186 checks, 186 passed, 0 failed.** Every module's document test cases + integration + curriculum checks. Mutation check: breaking two rules made 5 tests fail.
**[SCREENSHOT]** of `java DineSyncTests` final lines.
*Notes:* "We also run each module on its own with `java ModuleName`."

### 16. Genuine execution and GitHub evidence
**[SCREENSHOT]** of the program running (receipt or token) · **[SCREENSHOT]** of the GitHub repository page and commit history.
*Notes:* Show the repository: `src/`, `docs/`, README.

### 17. Limitations and future roadmap
Limitations: in-memory data, simulated sensors and payments, single console user.
Roadmap: Phase 2 storage → 3 GUI/web → 4 multi-terminal → 5 online ordering and payments → 6 production platform with real sensors.
*Notes:* "These are proposals; the current project is the Phase 1 Core Java prototype."

### 18. Conclusion and Q&A
DineSync brings order taking, kitchen, billing, feedback and reporting into one connected, validated Java program that covers CO1–CO6 of our course.
**Thank you. Questions?**
