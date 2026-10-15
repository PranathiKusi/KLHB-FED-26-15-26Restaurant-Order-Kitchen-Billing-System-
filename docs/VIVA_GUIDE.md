# DineSync Viva Guide: How the Project Works, Explained Simply

This guide is for the team to read before the expo and the faculty viva. It explains the project from the beginning in first-year language.

---

## 1. The big idea in one paragraph

A restaurant has four kinds of staff: a **waiter** takes the order, a **chef** cooks it, a **cashier** bills it, and an **admin** (manager) watches the numbers. DineSync is one Java program that all four use, each with their own menu after logging in. An order is typed in **once** and then moves on its own: waiter → kitchen queue → token display → bill → feedback → daily report. Around this flow, "smart" modules keep an 8-bit table register, simulate kitchen safety sensors, and calculate the electricity bill.

## 2. Folder tree

```
DineSync repository
├── src/                       ← every .java file (no packages, so "javac *.java" compiles everything)
│   ├── Main.java              ← program starts here
│   ├── ConsoleUI.java         ← all menus/screens
│   ├── InputHelper.java       ← keyboard input with Scanner (never crashes on bad input)
│   ├── Colors.java            ← colours + symbols (₹ or Rs.)
│   ├── Restaurant.java        ← creates and connects all services (one shared copy of the data)
│   ├── DemoData.java          ← optional labelled demo data (java Main --demo)
│   ├── ConceptShowcase.java   ← "Java concepts lab" (supporting demonstrations)
│   ├── DineSyncTests.java     ← 186 automated tests
│   ├── model classes          ← Staff, MenuItem, VegItem, NonVegItem, Beverage, Customer,
│   │                            Order, OrderItem, Bill, Billable, Feedback, Appliance
│   ├── service classes        ← AuthService, MenuService, CustomerService, OrderService, Kitchen,
│   │                            BillingService, FeedbackService, ReportService, FileStore, DateHelper
│   ├── monitor classes        ← TableRegister, Sensor, AbstractSensor, TemperatureSensor,
│   │                            GasLeakSensor, SmokeSensor, HumiditySensor, KitchenMonitor,
│   │                            SevenSegment, EnergyMeter
│   └── 38 exception classes   ← DineSyncException + one class per error (InvalidPinException ...)
├── data/menu.csv              ← starting menu (read from file at start-up)
├── demo/                      ← scripted demo input and its real output
└── docs/                      ← this guide and the other documents
```

**Why no packages?** The team needed every file to compile from Notepad + Command Prompt with `javac *.java` and to upload straight into `src/` on GitHub. The design still has five **layers** (UI, model, service, monitor, exception); they are just in one folder. (Java's own packages *are* used, e.g. `import java.util.ArrayList;`.)

## 3. What each kind of class does

| Layer | Job | Example |
|---|---|---|
| **UI** | Talk to the user: print menus, read input | `ConsoleUI.takeOrder()` asks for dish numbers |
| **Model** | Hold data about one thing | an `Order` object holds its items, table, status |
| **Service** | Apply the rules | `OrderService.addItem()` refuses Chicken 65 for a veg-only customer |
| **Monitor** | "Hardware-like" modules | `TableRegister` stores 8 tables in one `int` |
| **Exception** | Name every possible mistake | `InvalidQuantityException("qty must be 1-10")` |

**Golden rule of the design:** the UI never decides anything. It asks a service, and the service either does the job or **throws an exception** that the UI prints. So the same rule works in the menus, the tests and the standalone demos.

## 4. The four clusters and the 13 modules

| Cluster | Modules | Story |
|---|---|---|
| A: Identity, Menu, Customers & Tables | M01, M02, M03, M09 | Who is working, what we sell, who the guest is, where they sit |
| B: Ordering, Kitchen & Token | M04, M05, M11 | Take the order, cook it in the right order, show the token |
| C: Billing, Checksum & Feedback | M06, M12, M07 | Bill correctly, give a bill ID that cannot be mistyped, collect ratings |
| D: Monitoring, Energy & Analytics | M10, M13, M08 | Kitchen safety, electricity cost, daily reports |

## 5. End-to-end data flow (follow one order)

1. **Login (M01)**: `AuthService.login(1, "W104", "4821")` returns the `Staff` object for Kiran.
2. **Customer (M03)**: `CustomerService.checkIn("9123456780")` finds Priya in `Customer[]`; `visits` becomes 14; `14 % 7 == 0`, so loyalty is ON.
3. **Table (M09)**: `TableRegister.suggest(5)` returns 6; `allocate(6, 5)` does `reg |= 1 << 2`, so `0x4A` becomes `0x4E`.
4. **Order (M04)**: `OrderService.startOrder(...)` creates a draft `Order`; each `addItem` checks the rules; `placeOrder` gives it ID **2001**, sets status PLACED, and calls `kitchen.enqueue(order)`.
5. **Kitchen (M05)**: the chef calls `advance(2001)` twice: PLACED → PREPARING → READY. On READY, `SevenSegment.render("101", 3)` draws the token (**M11**).
6. **Served**: the waiter calls `advance(2001)` once more → SERVED, and the order leaves the kitchen queue.
7. **Bill (M06 + M12)**: `BillingService.pay(cashier, 2001, 900.0, 2)` calculates ₹869.40, checks the cash, makes bill ID **2001-9**, marks the order BILLED, and calls `tables.release(6)`.
8. **Feedback (M07)**: `FeedbackService.submit("2001-9", 5, {5,4,5,4}, "...")` verifies the bill ID and updates the average.
9. **Report (M08)**: `ReportService.summary(today)` loops over the real `Bill[]` and adds up revenue, so the report always equals the bills.

All services share **one** `Restaurant` object, so there is exactly one copy of each order and bill.

## 6. Key code, line by line

### 6.1 Recursion: the bill check digit (`BillingService.weightedSum`)
```java
public static int weightedSum(int number, int weight) {
    if (number == 0) {                 // BASE CASE: no digits left, so stop
        return 0;
    }
    return (number % 10) * weight      // last digit x its weight
         + weightedSum(number / 10, weight + 1);   // RECURSIVE CASE: the rest of the digits, next weight
}
```
* `number % 10` gives the last digit (2001 % 10 = 1).
* `number / 10` removes it (2001 / 10 = 200, because integer division drops the fraction).
* Each call has one digit fewer, so `number` eventually becomes 0 and the recursion **must** end.
* 2001 → 1×1 + 0×2 + 0×3 + 2×4 = 9 → check digit 9 → `2001-9`.

### 6.2 Bitwise register (`TableRegister.allocate` / `release`)
```java
public static int mask(int table) { return 1 << (TABLES - table); }  // T6 -> 1 << 2 = 0000 0100
...
if (isOccupied(table)) throw new TableOccupiedException(...);       // (reg & mask) != 0 means bit is 1
if (SEATS[table - 1] < party) throw new TableCapacityException(...);
reg |= mask(table);      // OR switches ONE bit on:  0100 1010 | 0000 0100 = 0100 1110
...
reg &= ~mask(table);     // AND with NOT switches it off: 0100 1110 & 1111 1011 = 0100 1010
```

### 6.3 Billing arithmetic (`BillingService.calculate`)
```java
double subtotal = b.getSubtotal();                // 920.00 (Billable interface: works for any Billable)
double discount = b.getDiscount();                // 92.00 on a loyalty visit, else 0
double taxable  = Bill.round2(subtotal - discount);            // 828.00
double cgst     = Bill.round2(taxable * Billable.CGST_RATE);   // 20.70
double sgst     = Bill.round2(taxable * Billable.SGST_RATE);   // 20.70
double total    = Bill.round2(taxable + cgst + sgst);          // 869.40
```
`round2` is `Math.round(value * 100.0) / 100.0`. It rounds to paise so the displayed numbers always add up.

### 6.4 Exceptions that do not stop the program (`KitchenMonitor.scanOnce`)
```java
for (Sensor s : sensors) {                 // the array holds 4 DIFFERENT sensor classes
    double reading = s.read();             // which read()? decided at run time = polymorphism
    try {
        s.check(reading);                  // throws SensorAlertException if reading > limit
        out.accept(label + " OK");
    } catch (SensorAlertException e) {     // caught HERE, so the loop continues with the next sensor
        alerts++;
        logAlert(date, time, e);
        out.accept(label + " ALERT -> " + e.getAction());
    }
}
```

### 6.5 Kitchen priority (`Order.compareTo`)
```java
public int compareTo(Order other) {
    if (this.urgent != other.urgent) {
        return this.urgent ? -1 : 1;                  // negative = "I come first"
    }
    return Long.compare(this.sequence, other.sequence);   // older (smaller sequence) first
}
```
`Arrays.sort(copy)` in `Kitchen.getActiveSorted()` calls this method to sort the queue.

### 6.6 Never crashing on bad input (`InputHelper.readInt`)
```java
while (true) {
    String s = readLine(prompt);
    try {
        return Integer.parseInt(s);          // "abc" throws NumberFormatException
    } catch (NumberFormatException e) {
        System.out.println("'" + s + "' is not a whole number");   // tell the user and ask again
    }
}
```

## 7. Where the beginner Java concepts appear

| Concept | Example in DineSync |
|---|---|
| Data types and variables | `double price`, `int qty`, `char` y/n, `boolean vegOnly`, `long sequence` (`Order`) |
| Operators and expressions | GST formula; `%` for loyalty; bitwise `\|=` `&=` `~` `<<` in `TableRegister` |
| Scanner | `InputHelper` (all input) |
| Control structures | `switch` menus in `ConsoleUI`; `do-while` role menus; nested `if` in `AuthService.login` |
| Methods | one method per operation; overloaded `pay(...)` and `readInt(...)` |
| Arrays | `MenuItem[30]`, `Customer[100]`, `Order[]`, `Bill[]`, 2-D `String[10][3]` and `double[24][3]` |
| Inheritance | `VegItem extends MenuItem`; `TemperatureSensor extends AbstractSensor`; exception hierarchy |
| Strings | ID/phone/date validation; `split`, `substring`, `indexOf`, `StringBuilder` receipts |
| Exception handling | 38 exception classes; try/catch everywhere; `finally` in `runScans` and `Main` |
| Overloading / overriding | overloading: `pay`, `readInt`, `search`, constructors. Overriding: `isVeg`, `getTypeLabel`, `kitchenStation`, `toString` |

The complete topic-by-topic list with class and method names is in `CURRICULUM_MATRIX.md`.

## 8. Common mistakes we avoided

| Mistake | What could go wrong | How DineSync avoids it |
|---|---|---|
| Storing the mobile number as `int` | 10-digit numbers overflow `int`; leading zeros lost | Stored as `String` |
| Storing the PIN as `int` | PIN `0427` would become `427` | Stored as `String`, checked for 4 digits |
| Comparing Strings with `==` | Compares objects, not text | `equals` / `equalsIgnoreCase` (`==` used only on purpose, for "same object") |
| Integer division in averages | `9 / 2` = 4 | `(double) total / n` |
| Floating-point money errors | 20.700000001 | `round2` at each step |
| Array index out of bounds | crash | capacity checks before every insert; item n is at index n-1 |
| Recursion without a base case | `StackOverflowError` | `if (number == 0) return 0;` |
| Empty `catch` blocks | errors silently ignored | every catch prints or logs a message |
| A success message after a failed operation | user is misled | the bill is created only after all payment checks pass |
| Two copies of the same order | the kitchen and the cashier disagree | one `Order` object, referenced from both arrays |
| Changing an order's price after a re-price | old bills change | `OrderItem` copies the unit price when the dish is added |

## 9. Viva questions and answers

**Basics (CO1)**
1. *What happens when you run `javac *.java`?* The compiler turns every `.java` source file into a `.class` file containing bytecode. `java Main` starts the JVM, which runs `Main.main`.
2. *Why `double` for money and not `float`?* `double` is more precise. We still round to 2 decimals with `round2`.
3. *Explain `14 % 7 == 0`.* `%` is the remainder. The remainder of 14 ÷ 7 is 0, so the 14th visit is a loyalty visit.
4. *What is `0x4A`?* A hexadecimal literal: 4×16 + 10 = 74 = binary 0100 1010.
5. *What does `reg |= 1 << 2` do?* `1 << 2` is 0000 0100. OR-ing it sets bit 2 to 1 (table T6 becomes busy) and leaves the other bits unchanged.
6. *Implicit vs explicit casting?* `int` to `double` happens automatically (widening). `(int) 434.7` needs an explicit cast and drops the fraction.

**Control flow (CO2)**
7. *Where do you use `switch`?* For every menu (`ConsoleUI`) and to pick the subclass in `MenuService.create`.
8. *`while` vs `do-while`?* `do-while` runs at least once. The role menus must show at least once, so they use `do-while`.
9. *Where are `break` and `continue` used?* `continue` skips busy tables in `TableRegister.suggest`. `break` stops the ETA count when we reach the order itself.
10. *How does the 3-attempt lock end the loop?* Each wrong PIN increases `failedAttempts`. At 3 the account is locked and an `AccountLockedException` sends the user back to the main menu.

**Methods, recursion, arrays (CO3)**
11. *Show method overloading.* `BillingService.pay` has three versions: `(Staff, int, double, int)` for cash, `(Staff, int, String, int)` for UPI, `(Staff, int, int)` for card. Java picks one by the argument types.
12. *What is the base case in your recursion?* `number == 0` in `weightedSum`, and `s.length() >= width` in `SevenSegment.padLeft`.
13. *Why arrays and not ArrayList for the main data?* The project specification asks for fixed-capacity arrays (menu 30, customers 100, appliances 15), and the limits are part of the rules.
14. *Where is a 2-D array used?* `String[10][3]` holds the 3 rows of each digit. `double[24][3]` is the hourly report (rows = hours, columns = orders, items, revenue).
15. *Which search and sort algorithms did you write yourself?* Linear search (staff, customers, orders) and a selection sort for the top-N dishes. From the library we use `Arrays.binarySearch` (bills) and `Arrays.sort` (kitchen queue).

**OOP and exceptions (CO4)**
16. *Why is `MenuItem` abstract?* There is no such thing as a plain "menu item"; every dish is Veg, Non-Veg or a Beverage. `abstract` stops anyone creating a plain `MenuItem` and forces subclasses to implement `isVeg()` and similar methods.
17. *Abstract class vs interface here?* `Sensor` is an interface (a promise of methods). `AbstractSensor` is an abstract class that implements it and shares the common code. Each sensor only adds its own auto-action.
18. *Show runtime polymorphism.* `for (Sensor s : sensors) s.read();`. The array type is `Sensor`, but each element's own class decides which `read`/`getAutoAction` runs.
19. *What is encapsulation in your code?* Fields are `private`; for example `Order.lines` can only change through `addItem`, which checks the limits. `getLines()` returns a copy.
20. *Checked vs unchecked exception?* `DineSyncException extends Exception` is checked, so the compiler forces try/catch or `throws`. `InvalidMenuChoiceException extends RuntimeException` is unchecked.
21. *Where is `finally` used and why?* In `KitchenMonitor.runScans`, it always prints "Scan session closed", even if the scan was interrupted. In `Main`, it always closes the `Scanner`.
22. *What is multi-catch?* One catch for several types: `catch (InvalidStaffIdException | AccountLockedException e)` in `ConsoleUI.login`.
23. *Error vs exception?* Errors (like `StackOverflowError` or `OutOfMemoryError`) are serious JVM problems you normally don't catch. Exceptions are recoverable problems that the program handles.

**Strings, algorithms, files (CO5)**
24. *How do you split a bill ID?* `indexOf('-')` finds the dash; `substring(0, dash)` is the number and `substring(dash + 1)` is the check digit; `Integer.parseInt` converts them. A `NumberFormatException` becomes an `InvalidBillFormatException`.
25. *Why `StringBuilder`?* Strings are immutable, so `+` in a loop creates many objects. `StringBuilder` changes one buffer. `StringBuffer` (used for the alert log) is the thread-safe version.
26. *How does Arrays.binarySearch find a bill?* Bill numbers are stored in increasing order (2001, 2002, ...), so the array is sorted and the search halves the range each step.
27. *What file I/O does DineSync do?* It reads `data/menu.csv` line by line (skipping and reporting malformed lines), appends to `audit.log`, and writes and reads back report files. All of this uses `Files`, `Path` and try-with-resources.
28. *Do orders survive a restart?* **No.** They are in arrays in memory. Only the menu file, the audit log and exported reports are files. Saving everything is listed as future scope.

**Collections and streams (CO6)**
29. *Where is a HashSet used and why?* `FeedbackService` stores bill IDs that already have feedback. `contains()` instantly answers "already given?".
30. *Explain this stream:* `bills.stream().map(b -> b.getCgst() + b.getSgst()).reduce(0.0, (a, b) -> a + b)`. It turns each bill into its GST amount, then adds them all, starting from 0.0.
31. *What does `groupingBy` do in your report?* It groups sold items by category name into a `TreeMap` (sorted) and adds their quantities.
32. *What is `Optional`?* A box that may or may not hold a value. `MenuService.findByName` returns `Optional<MenuItem>` so the caller must handle "not found".
33. *What is a method reference?* A short lambda: `System.out::println` means `line -> System.out.println(line)`.

**Project questions**
34. *What is "IoT" about it if there is no hardware?* The sensors, buzzer and seven-segment display are modelled in software with the same logic a microcontroller would use (thresholds, bit codes, registers). Real hardware is future scope.
35. *How do you know your numbers are right?* 186 automated tests reproduce every number in the project document (₹869.40, 2001-9, 0x4E, 24 min, 26.70 kWh, ₹173.60) and check every error case. A mutation check showed the tests fail when a rule is broken.

## 10. Five-minute live demo

See `DEMO_SCRIPT.md`. In short: `java Main --demo --fast`, then admin adds Mirchi Bajji, waiter serves Priya (T6, order #2001), chef cooks (token appears), cashier bills ₹869.40 → `2001-9`, waiter takes feedback, admin shows the daily report and energy cost.

## 11. Limitations to admit honestly

In-memory data only; simulated sensors and payments; a single console user; reports cover the current run; tokens are numbered from 101. See the README's "Known limitations".
