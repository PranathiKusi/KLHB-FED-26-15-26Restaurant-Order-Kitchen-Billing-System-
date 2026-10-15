# Complete Curriculum Coverage Matrix

**Source:** Course handout *26SC1101E – Problem Solving Using Programming (JAVA), K L Deemed to be University, 2026-27 Odd Sem*. Topics are taken from the **Syllabus** paragraph (Modules 1–6), the **Course Outcomes CO1–CO6**, the **Course Outcome Indicators**, and the **session-wise teaching plan** (sessions 1–30). The faculty **Rubrics and Evaluation Form** items are mapped at the end.

**How to read the columns**
- **Where**: the class and method where the topic is used (`Class.method`). All files are in `src/`.
- **Verified by**: an automated test in `DineSyncTests` (the section name is given), the recorded run in `demo/demo-transcript.txt`, a standalone `main()` run, or the `ConceptShowcase` output.
- **Kind**: **Integrated** means it is part of the real restaurant workflow. **Supporting** means a small demonstration in `ConceptShowcase` or a standalone `main()`, used where the topic has no natural place in the restaurant flow.
- **Status** uses the four statuses from the project brief. Every row below was compiled and executed.

> The handout numbers its content as **Modules 1–6**, and those line up with **CO1–CO6** in the CO table. The rubric uses the same CO1–CO6 titles. Both labels are shown.

---

## Module 1 / CO1: From Problem to Program (Computational Thinking & Java Foundations)

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 1.1 | Introduction to problem solving, computational thinking, IPO model | Each module is described as Input → Process → Output | `docs/ALGORITHMS.md` (IPO table per module) | Document review | Integrated | Implemented and tested |
| 1.2 | Problem solving using algorithm & flowchart; pseudocode | Algorithms, pseudocode and Mermaid flowcharts for table allocation, order placement, billing, checksum, reports | `docs/ALGORITHMS.md` | Matches code; outputs confirmed by tests | Integrated | Implemented and tested |
| 1.3 | Stepwise refinement, desk checking, trace tables (sessions 2–3) | Order flow broken into `startOrder → addItem → placeOrder`; trace tables for `weightedSum` and the selection sort | `OrderService`, `docs/ALGORITHMS.md` | `[M04]` tests; trace tables match program output | Integrated | Implemented and tested |
| 1.4 | Java environment, JDK/JRE/JVM, edit–compile–run cycle | `javac *.java` → `.class` bytecode → `java Main` | `README.md`, `scripts/` | Compiled and run with JDK 21 using `--release 17` | Integrated | Implemented and tested |
| 1.5 | Structure of a Java program (class, `main`, statements, comments, blocks) | Every module class has a commented `main()` | `Main.main`, `AuthService.main` … `EnergyMeter.main` | All 14 standalone `main` methods executed | Integrated | Implemented and tested |
| 1.6 | Primitive data types | `int` IDs and quantities, `double` money, `char` y/n answers, `boolean` flags, `long` order sequence | `Order` fields, `InputHelper.readYesNo`, `Order.sequence` | `[M04]`, `[M06]` | Integrated | Implemented and tested |
| 1.7 | Variables, identifiers, literals | Descriptive names; hex literals `0x4A` and `0x7E`; `char` literals; `String` literals | `TableRegister`, `SevenSegment.CODES` | `[M09]`, `[M11]` | Integrated | Implemented and tested |
| 1.8 | Constants | `static final` such as `MAX_ITEMS = 20`, `CAPACITY = 30`, interface constants `CGST_RATE` | `Order`, `MenuService`, `Billable` | `[M02]`, `[M04]`, `[M06]` | Integrated | Implemented and tested |
| 1.9 | Arithmetic operators | Subtotal, tax, change, split, kW, kWh, ETA | `BillingService.calculate`, `Appliance.getPowerKw`, `Kitchen.eta` | `[M06]` 869.40; `[M13]` 26.70; `[M05]` ETA 24 | Integrated | Implemented and tested |
| 1.10 | Relational operators | Range checks (`price < 10`, `qty > 10`, `reading > limit`) | All `validate`/`check` methods | Boundary tests in every module | Integrated | Implemented and tested |
| 1.11 | Logical operators `&&` `\|\|` `!` | Diet rule, PIN format, Beverage/category rule | `MenuService.allowedFor`, `AuthService.login`, `MenuService.addDish` | `[M02]`, `[M03]`, `[M01]` | Integrated | Implemented and tested |
| 1.12 | Assignment and compound assignment (`+=`, `\|=`, `&=`) | Totals, table register updates | `Order.getSubtotal`, `TableRegister.allocate/release` | `[M09]` 0x4A→0x4E→0x4A | Integrated | Implemented and tested |
| 1.13 | Ternary operator | Labels, plural forms, Unicode/ASCII symbols | `Colors.tick`, `Order.toString`, `AuthService.login` | Visible in the transcript | Integrated | Implemented and tested |
| 1.14 | Unary operators (`++`, `--`, `!`, `~`) | Counters, `~mask` to clear a bit | `Customer.checkIn`, `TableRegister.release` | `[M03]` loyalty pattern; `[M09]` | Integrated | Implemented and tested |
| 1.15 | Special operators (`instanceof`, `.`, `[]`, `new`) | `instanceof` before a downcast | `ConsoleUI.simulateOverheat`; tests `[M10]` polymorphism | Transcript (overheat injected); `[M10]` | Integrated | Implemented and tested |
| 1.16 | Bitwise operators (`&` `\|` `~` `<<` `>>` `^`) | 8-bit table register, segment bits | `TableRegister.mask/isOccupied/allocate/release`, `SevenSegment.segmentOn` | `[M09]`, `[M11]`; `ConceptShowcase` shows `^` | Integrated | Implemented and tested |
| 1.17 | Typecasting: implicit widening, explicit narrowing | `(double) total / n`; `(int)` in hourly report; `(char)`; `Math.round` long→double | `FeedbackService.averageRating`, `ReportService.hourly`, `Bill.round2` | `[M07]` 4.5; `ConceptShowcase` | Integrated | Implemented and tested |
| 1.18 | Character conversion | `digit - '0'`, `Character.isDigit`, `Character.toUpperCase` | `SevenSegment.render`, `AuthService.validateStaffId` | `[M11]`, `[M01]` | Integrated | Implemented and tested |
| 1.19 | Expression evaluation: precedence and associativity | Tax and energy formulas; explicit demo `2 + 3 * 4`, `20 - 6 - 4` | `EnergyMeter.cost`, `ConceptShowcase.typesAndOperators` | `[M13]`; showcase output | Integrated and Supporting | Implemented and tested |
| 1.20 | Console I/O: `print`, `println`, `printf`/`String.format` | Receipts, tables, reports | `BillingService.receipt`, `ReportService.*`, `ConsoleUI.billOrder` | Transcript | Integrated | Implemented and tested |
| 1.21 | Reading typed input with `Scanner` | All keyboard input | `InputHelper.readLine/readInt/readDouble/readYesNo`, `Main.main` | Scripted run `demo-input.txt` → transcript | Integrated | Implemented and tested |

## Module 2 / CO2: Control Flow (Selection & Iteration)

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 2.1 | `if` | Range validation | Every `validate…`/`check…` method | All modules | Integrated | Implemented and tested |
| 2.2 | `if-else` | Dine-in vs takeaway; found vs new customer | `OrderService.startOrder`, `ConsoleUI.identifyCustomer` | `[M04]`; transcript | Integrated | Implemented and tested |
| 2.3 | Nested `if` | Login: ID valid → staff found → not locked → PIN | `AuthService.login`, `Kitchen.changeStatus` | `[M01]`, `[M05]` | Integrated | Implemented and tested |
| 2.4 | `if-else-if` ladder | Payment-mode handling; sort choice; receipt lines | `ConsoleUI.billOrder`, `BillingService.receipt`, `ConsoleUI.updateOrder` | `[M06]`; transcript | Integrated | Implemented and tested |
| 2.5 | `switch` with `default` and `break`; menu-driven programming | All role menus, role dispatch, dish factory, option parsing | `ConsoleUI.run/login/waiterMenu…`, `MenuService.create`, `Main.main` | Transcript | Integrated | Implemented and tested |
| 2.6 | Compound statements (blocks) | Multi-statement bodies everywhere | All classes | Compiles and runs | Integrated | Implemented and tested |
| 2.7 | `while` loop; sentinel-controlled iteration | File reading until `null`; order entry until dish `0`; main menu | `FileStore.loadMenuCsv`, `ConsoleUI.takeOrder`, `ConsoleUI.run` | `[CO5]` file test; transcript | Integrated | Implemented and tested |
| 2.8 | `do-while` | Role menus; non-blank input | `ConsoleUI.waiterMenu` (etc.), `InputHelper.readNonBlank` | Transcript | Integrated | Implemented and tested |
| 2.9 | `for` loop | Array traversal | `Order.getTotalQuantity`, `EnergyMeter.getTotalUnits`, … | Many tests | Integrated | Implemented and tested |
| 2.10 | Enhanced `for` (for-each) | Sensor scan, order lines, bills | `KitchenMonitor.scanOnce`, `ReportService.revenue` | `[M10]`, `[M08]` | Integrated | Implemented and tested |
| 2.11 | `break` | Stop searching once found; stop ETA count at this order | `MenuService.menuFor`, `Kitchen.eta`, `Customer.topFavourites` | `[M05]` ETA 24; `[M03]` | Integrated | Implemented and tested |
| 2.12 | `continue` | Skip busy/small tables; skip comment lines in CSV; skip empty hours | `TableRegister.suggest`, `FileStore.loadMenuCsv`, `ReportService.hourly` | `[M09]`, `[CO5]`, `[M08]` | Integrated | Implemented and tested |
| 2.13 | Infinite loops (controlled `while (true)` + `return`/`break`) | Input retry loops | `InputHelper.readInt`, `ConsoleUI.allocateTable` | Transcript (invalid input re-asked) | Integrated | Implemented and tested |
| 2.14 | Nested loops; pattern generation and report generation | Seven-segment digits (rows × digits); floor plan; menu ordering | `SevenSegment.render`, `TableRegister.floorPlan`, `MenuService.menuFor` | `[M11]`; transcript | Integrated | Implemented and tested |
| 2.15 | Matrix processing using nested loops | Hourly 24 × 3 report, row and column totals | `ReportService.hourly/hourlyMatrix` | `[M08]` hourly totals | Integrated | Implemented and tested |
| 2.16 | Statistics using iteration (counters, accumulators, max) | Revenue, counts, averages, highest appliance | `ReportService.revenue`, `FeedbackService.averageRating`, `EnergyMeter.highest` | `[M08]`, `[M07]`, `[M13]` | Integrated | Implemented and tested |
| 2.17 | Reasoning about loop termination (CO2 BTL4) | Retry loops end only on valid input; recursion base cases; 3-attempt lock | `InputHelper`, `AuthService.login`, `BillingService.weightedSum` | `[M01]` lock; `[M12]` | Integrated | Implemented and tested |

## Module 3 / CO3: Decomposition with Methods, Recursion & Arrays

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 3.1 | Modular programming; method definition and invocation | One method per operation; services call each other | All services | All tests | Integrated | Implemented and tested |
| 3.2 | Parameter passing | Primitives by value; object references (e.g. `Order` passed to `BillingService.calculate`) | `OrderService.addItem(Order, int, int)` | `[M04]` merge test changes the same `Order` | Integrated | Implemented and tested |
| 3.3 | Return values / `void` methods | `double[]` amounts, `int[]` favourites, `void` setters | `BillingService.calculate`, `Customer.topFavourites` | `[M06]`, `[M03]` | Integrated | Implemented and tested |
| 3.4 | Method overloading | `pay(cash)`, `pay(UPI)`, `pay(card)`; `readInt(prompt)` and `readInt(prompt,min,max)`; `search(String)` and `search(int)`; `requireRole` with 1 or 2 roles | `BillingService.pay`, `InputHelper.readInt`, `MenuService.search`, `AuthService.requireRole` | `[M06]` UPI and card; `[M02]` search | Integrated | Implemented and tested |
| 3.5 | Recursion: base case and recursive case | Weighted digit sum for the bill check digit; zero-padding of tokens | `BillingService.weightedSum`, `SevenSegment.padLeft` | `[M12]` weightedSum(2001)=9; `[M11]` "7"→"007" | Integrated | Implemented and tested |
| 3.6 | 1-D arrays: declaration, creation, initialization | `Staff[]`, `MenuItem[30]`, `Customer[100]`, `Order[]`, `Bill[]`, `Feedback[]`, `Appliance[15]`, `Sensor[4]`, `String[] alertLog`, `int[] SEATS` | All services | Capacity tests in `[M02]`, `[M03]`, `[M13]` | Integrated | Implemented and tested |
| 3.7 | Traversal, updating and manipulating elements | Merge order lines; shift the queue left on removal | `Order.addItem`, `Kitchen.remove` | `[M04]` merge; `[M05]` served leaves queue | Integrated | Implemented and tested |
| 3.8 | Linear search | Staff by ID, customer by phone, order by ID | `AuthService.findById`, `CustomerService.findByPhone`, `OrderService.findOrder` | `[M01]`, `[M03]`, `[M05]` | Integrated | Implemented and tested |
| 3.9 | Summation, average, counting | Subtotal, average rating, alert count, occupied count | `Order.getSubtotal`, `FeedbackService.averageRating`, `KitchenMonitor.countAlertsOn`, `TableRegister.occupiedCount` | `[M04]`, `[M07]`, `[M10]`, `[M09]` | Integrated | Implemented and tested |
| 3.10 | Sorting records (selection / bubble style) | Selection sort for top-N dishes; selection-style top-3 favourites | `ReportService.topIndices`, `Customer.topFavourites` | `[M08]` topIndices; `[M03]` favourites | Integrated | Implemented and tested |
| 3.11 | Merit list / ranking | Top-dishes ranking report | `ReportService.topDishes` | `[M08]` Veg Biryani first | Integrated | Implemented and tested |
| 3.12 | 2-D arrays: declaration, matrix representation | `String[10][3]` digit patterns; `double[24][3]` hourly matrix; `Object[][]` seed menu | `SevenSegment.PATTERNS`, `ReportService.hourlyMatrix`, `MenuService.loadDefaultMenu` | `[M11]`, `[M08]` | Integrated | Implemented and tested |
| 3.13 | Row-wise and column-wise processing | Hourly rows printed, columns totalled | `ReportService.hourly` | `[M08]` hourly totals | Integrated | Implemented and tested |
| 3.14 | 2-D matrix arithmetic | Matrix addition (lunch + dinner plates) | `ConceptShowcase.matrixDemo` | Showcase output | Supporting | Implemented and tested |

## Module 4 / CO4: Object-Oriented Modelling, Design & Exception Handling

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 4.1 | Classes as blueprints, objects as instances; fields and methods | `Staff`, `MenuItem`, `Customer`, `Order`, `Bill`, … | `src/*.java` | All tests | Integrated | Implemented and tested |
| 4.2 | Object creation with `new`; references and `null` | `findById` returns `null` when not found; `Double forcedNext` can be `null` | `AuthService.findById`, `AbstractSensor.read` | `[M01]` unknown ID; `[M10]` | Integrated | Implemented and tested |
| 4.3 | Default constructor | No-arg constructors: `AuthService()`, `TemperatureSensor()`; implicit default: `TableRegister`, `MenuService`, `KitchenMonitor` | as listed | Constructed in every test | Integrated | Implemented and tested |
| 4.4 | Parameterized constructor | `Order(type, table, customer, waiterId)`, `Bill(...)`, `Appliance(...)` | model classes | `[M04]`, `[M06]`, `[M13]` | Integrated | Implemented and tested |
| 4.5 | Constructor overloading and `this(...)` | `Customer(phone,name)` → `this(phone,name,false)`; `MenuItem` five/six-argument constructors | `Customer`, `MenuItem` | `[CURRICULUM]` constructor overloading | Integrated | Implemented and tested |
| 4.6 | `this` keyword | Field vs parameter names | All constructors | Compiles and runs | Integrated | Implemented and tested |
| 4.7 | Encapsulation: private fields, accessors/mutators, information hiding | All model fields `private`; getters return copies of arrays | `Order.getLines`, `Feedback.getDishRatings`, `MenuService.getAll` | `[CURRICULUM]` encapsulation | Integrated | Implemented and tested |
| 4.8 | Access modifiers (`private`, default, `protected`, `public`) | `protected` constructors in `MenuItem`/`AbstractSensor`; package-private `SevenSegment.padLeft` | as listed | Compiles and runs | Integrated | Implemented and tested |
| 4.9 | Instance vs static members; static methods; constants | **static** methods for checks that need no object (`CustomerService.validatePhone`, `BillingService.checkDigit`); **instance** fields that belong to one object (`OrderService.nextOrderId`, `Kitchen.nextToken`); **static final** constants (`Order.MAX_ITEMS`) | `CustomerService`, `BillingService`, `OrderService`, `Kitchen`, `Colors` | `[M03]`, `[M12]`, `[M04]` | Integrated | Implemented and tested |
| 4.10 | `toString()` for object representation | `MenuItem`, `Order`, `Bill`, `Staff`, `Customer`, `Appliance`, `Feedback`, `OrderItem` | model classes | Transcript (menu lines, staff welcome) | Integrated | Implemented and tested |
| 4.11 | Inheritance: types | **Hierarchical:** `MenuItem` → `VegItem`/`NonVegItem`/`Beverage`; `AbstractSensor` → 4 sensors. **Multilevel:** `Exception` → `DineSyncException` → `InvalidPinException`. **Single:** `VegItem extends MenuItem` | model, monitor and exception classes | `[CURRICULUM]` inheritance | Integrated | Implemented and tested |
| 4.12 | Interfaces, "is-a" | `Billable` (Order, Bill), `Sensor` (4 sensors), `Comparable` (MenuItem, Order) | as listed | `[M06]`, `[M10]`, `[M05]` | Integrated | Implemented and tested |
| 4.13 | Abstract classes | `MenuItem`, `AbstractSensor` | as listed | `[M02]`, `[M10]` | Integrated | Implemented and tested |
| 4.14 | Method overriding | `isVeg`, `getTypeLabel`, `kitchenStation`, `getAutoAction`, `toString`, `compareTo`, `getSubtotal` | subclasses | `[CURRICULUM]` overriding | Integrated | Implemented and tested |
| 4.15 | Polymorphism and runtime binding | `MenuItem[]` holds 3 subclasses; `Sensor[]` holds 4; `s.read()` and `item.kitchenStation()` resolved at run time | `KitchenMonitor.scanOnce`, `Kitchen.display` | `[M10]` Sensor[] test; `[CURRICULUM]` runtime binding | Integrated | Implemented and tested |
| 4.16 | Programming to interfaces | `BillingService.calculate(Billable)`; `List<…>` and `Map<…>` declared types; `Consumer<String>` output | `BillingService`, `ReportService`, `KitchenMonitor` | `[M06]` | Integrated | Implemented and tested |
| 4.17 | Object relationships / composition (COI) | `Restaurant` HAS-A every service; `Order` HAS-A `OrderItem[]` and a `Customer`; `Bill` HAS-A `Order` | `Restaurant`, `Order`, `Bill` | `[INTEGRATION]` | Integrated | Implemented and tested |
| 4.18 | Packages (COI) | Java library packages imported (`java.util`, `java.util.function`, `java.util.stream`, `java.nio.file`). DineSync's own classes stay in the default package **at the team's request**, so `javac *.java` works from Notepad + cmd. See `DESIGN_DECISIONS.md` D1 | `import` lines | Compiles | Integrated | Partially implemented (own package declarations not used, by design) |
| 4.19 | Errors vs exceptions | `AssertionError` (an `Error`) in the tests; viva notes on `StackOverflowError` | `DineSyncTests`, `docs/VIVA_GUIDE.md` | Test runner | Supporting | Implemented and tested |
| 4.20 | Checked exceptions | 36 module exceptions extend `DineSyncException extends Exception` | `src/*Exception.java` | `[CURRICULUM]` checked vs unchecked | Integrated | Implemented and tested |
| 4.21 | Unchecked exceptions | `InvalidMenuChoiceException extends RuntimeException`; built-in `NumberFormatException` caught | `InputHelper.readInt`, `AuthService.validateStaffId` | `[M01]` role 5 | Integrated | Implemented and tested |
| 4.22 | `try` / `catch` / `finally` | `finally` closes the scan session and the `Scanner` | `KitchenMonitor.runScans`, `Main.main` | `[M10]` output includes "Scan session closed" | Integrated | Implemented and tested |
| 4.23 | Multi-catch | `catch (InvalidStaffIdException \| AccountLockedException e)` | `ConsoleUI.login`, `ConsoleUI.takeOrder`, `Kitchen.changeStatus` | Transcript | Integrated | Implemented and tested |
| 4.24 | `throw` | Every rule violation | All services | All exception tests | Integrated | Implemented and tested |
| 4.25 | `throws` | Method signatures declare checked exceptions | All services | Compiler-enforced | Integrated | Implemented and tested |
| 4.26 | Custom exceptions | 38 exception classes, one per documented error | `src/*Exception.java` | Every module's tests | Integrated | Implemented and tested |

## Module 5 / CO5: Strings, Library Algorithms & File I/O

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 5.1 | String abstraction; immutability | Names normalised into new Strings; immutability demo | `MenuService.toTitleCase`, `ConceptShowcase.stringsDemo` | `[CURRICULUM]` toTitleCase; showcase | Integrated and Supporting | Implemented and tested |
| 5.2 | Character representation; `length`, indexing `charAt` | Staff ID, phone, token, bill ID checks | `AuthService.validateStaffId`, `CustomerService.validatePhone`, `SevenSegment.validateToken` | `[M01]`, `[M03]`, `[M11]` | Integrated | Implemented and tested |
| 5.3 | `substring`, concatenation | Bill ID split; register nibbles; title case | `BillingService.parseBillId`, `TableRegister.toBinary` | `[M12]`, `[M09]` | Integrated | Implemented and tested |
| 5.4 | Comparison: `equals`, `equalsIgnoreCase`, `compareTo` | PIN check, duplicate dish, sorting | `Staff.pinMatches`, `MenuService.findByName`, `MenuItem.compareTo` | `[M01]`, `[M02]` | Integrated | Implemented and tested |
| 5.5 | `equals()` vs `==` (value vs reference) | `==` used deliberately for "same object" (`EnergyMeter.report`, `Kitchen.remove`); demo of both | `EnergyMeter.report`, `ConceptShowcase.stringsDemo` | Showcase output: `true` / `false` | Integrated and Supporting | Implemented and tested |
| 5.6 | Case conversion | Staff ID letter, title case, keyword search | `AuthService`, `MenuService.search` | `[M02]` search | Integrated | Implemented and tested |
| 5.7 | Searching in strings (`indexOf`, `contains`, `startsWith`) | Bill ID dash; menu search; CSV comments | `BillingService.parseBillId`, `MenuService.search`, `FileStore.loadMenuCsv` | `[M12]`, `[M02]`, `[CO5]` | Integrated | Implemented and tested |
| 5.8 | `StringBuilder` | Receipts, reports, kitchen display, segment display | `BillingService.receipt`, `ReportService.*`, `SevenSegment.render` | Transcript | Integrated | Implemented and tested |
| 5.9 | `StringBuffer` | Alert-log entries | `KitchenMonitor.logAlert` | `[M10]` alert log | Integrated | Implemented and tested |
| 5.10 | `split()`, `join()`, trimming, validation, tokenization of records | Date parts, CSV menu records, favourites line | `DateHelper.validate`, `FileStore.loadMenuCsv`, `CustomerService.favouritesText` | `[M08]`, `[CO5]`, `[M03]` | Integrated | Implemented and tested |
| 5.11 | `Arrays.sort()` | Kitchen priority (Comparable), menu by price (Comparator) | `Kitchen.getActiveSorted`, `MenuService.sortedByPrice` | `[M05]`, `[M02]` | Integrated | Implemented and tested |
| 5.12 | `Collections.sort()` | Sorting a `List<MenuItem>` A–Z | `ConceptShowcase.collectionsDemo` | Showcase output | Supporting | Implemented and tested |
| 5.13 | `Arrays.binarySearch()` | Find a bill by number in the sorted `billNumbers[]` | `BillingService.verify` | `[M12]` found / not found | Integrated | Implemented and tested |
| 5.14 | `Comparable` | `MenuItem` (by name), `Order` (urgent first, then oldest) | `MenuItem.compareTo`, `Order.compareTo` | `[M05]` queue order; `[CURRICULUM]` | Integrated | Implemented and tested |
| 5.15 | `Comparator` | `Comparator.comparingDouble(MenuItem::getPrice)`, `comparingInt` | `MenuService.sortedByPrice`, `ConceptShowcase.functionalDemo` | `[M02]` | Integrated | Implemented and tested |
| 5.16 | `java.nio.file` `Files` and `Path` | All file work | `FileStore` | `[CO5]` | Integrated | Implemented and tested |
| 5.17 | Buffered readers/writers | `Files.newBufferedReader/Writer` | `FileStore.loadMenuCsv/readLines/writeReport/appendAudit` | `[CO5]` | Integrated | Implemented and tested |
| 5.18 | Reading complete files; line-by-line processing | Menu CSV line by line; whole report read back | `FileStore.loadMenuCsv`, `FileStore.readLines` | `[CO5]` | Integrated | Implemented and tested |
| 5.19 | Appending data | Audit log with `StandardOpenOption.APPEND` | `FileStore.appendAudit` | `[CO5]` audit; `[INTEGRATION]` BILL line | Integrated | Implemented and tested |
| 5.20 | Handling missing files and malformed records | `NoSuchFileException`; wrong field count; bad numbers; invalid dishes skipped with messages | `FileStore.loadMenuCsv` | `[CO5]`: 2 loaded, 3 skipped; missing file | Integrated | Implemented and tested |
| 5.21 | try-with-resources | Every reader, writer and stream | `FileStore` | `[CO5]` | Integrated | Implemented and tested |

## Module 6 / CO6: Functional Programming with Collections & Streams

| # | Handout topic | DineSync use | Where | Verified by | Kind | Status |
|---|---|---|---|---|---|---|
| 6.1 | Collections framework map: `List`, `Set`, `Map` | `List<Bill>`, `Set<String>` of billed feedback, `Map<String,int[]>` staff stats | `ReportService`, `FeedbackService` | `[M08]`, `[M07]` | Integrated | Implemented and tested |
| 6.2 | `ArrayList` | Report bill lists, file lines, load messages | `ReportService.billsOn`, `FileStore.readLines` | `[M08]`, `[CO5]` | Integrated | Implemented and tested |
| 6.3 | `HashMap` | Dish-price lookup | `ConceptShowcase.collectionsDemo` | Showcase output | Supporting | Implemented and tested |
| 6.4 | `HashSet` | "Feedback already given for this bill?" | `FeedbackService.verifyForFeedback` | `[M07]` duplicate feedback | Integrated | Implemented and tested |
| 6.5 | Ordered map/set (`TreeMap`, `TreeSet`) | Staff report sorted by ID; rating distribution; category breakdown; sorted category set | `ReportService.staffReport`, `FeedbackService.ratingDistribution`, `ConceptShowcase` | `[M08]` staff; `[M07]` `{5=1}` | Integrated | Implemented and tested |
| 6.6 | Adding, removing, looking up, iterating | `put/get/contains/add`; `Iterator.remove` | `ReportService.staffReport`, `ConceptShowcase.collectionsDemo` | `[M08]`; showcase | Integrated and Supporting | Implemented and tested |
| 6.7 | Generics: typed collections and type safety | `List<Bill>`, `Map<String, Double>`, `Class<? extends Throwable>` | `ReportService`, `DineSyncTests.expect` | Compiles with `-Xlint:all` and no warnings | Integrated | Implemented and tested |
| 6.8 | Autoboxing / unboxing | `Double forcedNext`; `List<Integer>` ratings; `Map<String, Double>` | `AbstractSensor`, `FeedbackService.averageOn` | `[M10]`, `[M08]` | Integrated | Implemented and tested |
| 6.9 | Iterating: for-each, `Iterator` | `Iterator<Map.Entry<…>>` in the staff report | `ReportService.staffReport` | `[M08]` staff report | Integrated | Implemented and tested |
| 6.10 | Functional interfaces and lambda expressions | `Predicate` filters, `Consumer` scan output, test `Action` interface | `MenuService.filter/search`, `KitchenMonitor.runScans`, `DineSyncTests.Action` | `[M02]`, `[M10]` | Integrated | Implemented and tested |
| 6.11 | Method references | `System.out::println`, `MenuItem::getPrice`, `Bill::getPaymentModeName`, `Integer::intValue`, `Character::isDigit` | `ConsoleUI.runSensorScan`, `MenuService`, `ReportService`, `AuthService` | Transcript; `[M08]` | Integrated | Implemented and tested |
| 6.12 | `Predicate`, `Function`, `Consumer`, `Supplier`, `Comparator` in practice | Predicate: `MenuService.filter`; Function: `ReportService.paymentModes`; Consumer: `KitchenMonitor`; Supplier: `ConceptShowcase` (`DateHelper::today`); Comparator: `MenuService.sortedByPrice` | as listed | Tests and showcase | Integrated (Supplier: Supporting) | Implemented and tested |
| 6.13 | Creating streams from collections, arrays and files | `Arrays.stream(menu,…)`, `bills.stream()`, `Files.lines(...)` | `MenuService.findByName`, `ReportService`, `FileStore.countLinesContaining` | `[M02]`, `[M08]`, `[CO5]` | Integrated | Implemented and tested |
| 6.14 | Intermediate: `filter`, `map`, `sorted`, `distinct`, `limit` | `filter`/`map`/`flatMap` in reports; `sorted`, `distinct`, `limit` in the showcase | `ReportService.categoryBreakdown`, `ConceptShowcase.functionalDemo` | `[M08]`; showcase | Integrated and Supporting | Implemented and tested |
| 6.15 | Terminal: `forEach`, `collect`, `count`, `reduce` | `reduce` for GST total; `count` for file lines; `collect` for groups; `forEach` for search output | `ReportService.sumTax`, `FileStore.countLinesContaining`, `ConsoleUI.searchMenu` | `[M08]`, `[CO5]` | Integrated | Implemented and tested |
| 6.16 | Collectors: `toList`, `groupingBy`, `counting`, `joining` | Category breakdown, payment modes, rating distribution; `toList` in the showcase | `ReportService`, `FeedbackService.ratingDistribution` | `[M08]` summary; `[M07]` | Integrated | Implemented and tested |
| 6.17 | Reading file lines as a stream | Count audit-log lines containing "BILL" | `FileStore.countLinesContaining`, `ConsoleUI.auditLog` | `[CO5]`; transcript "BILL lines: 1" | Integrated | Implemented and tested |
| 6.18 | `Optional` (COI, CO5 indicator) | Dish lookup that may find nothing | `MenuService.findByName` | `[M02]` | Integrated | Implemented and tested |

## Additional items listed in the handout

| Item | How it is handled | Status |
|---|---|---|
| Industry tool: **JDK 21 (or latest LTS)** | Built and tested on JDK 21 with `--release 17`, so it also runs on JDK 17 | Implemented and tested |
| Industry tool: **Git & GitHub** | Repository with a staged commit history | Implemented |
| Industry tool: **JUnit 5** | **Not used.** The project brief asks for no third-party dependencies, so the 186 tests use a plain-Java runner (`DineSyncTests`) that runs with `javac *.java`. The same checks could be moved to JUnit 5 later. | Not implemented (deliberate; equivalent plain-Java tests provided) |
| Industry tool: **Maven** | Not used, for the same reason. The flat `javac *.java` build was requested by the team. | Not implemented (deliberate) |

---

## Faculty Rubric (End Project Review Form) → evidence

| Rubric row | Criterion | Where to show it |
|---|---|---|
| CO1: From Problem to Program (BTL3) | Algorithm, data types, expressions, I/O | `docs/ALGORITHMS.md`; `BillingService.calculate`; `InputHelper`; `TableRegister` bitwise |
| CO2: Control Flow (BTL4) | Selection, loops, termination | `ConsoleUI` menus (switch, do-while); `AuthService.login` (nested if, 3-attempt lock); `InputHelper` retry loops |
| CO3: Methods, Recursion & Arrays (BTL4) | Methods, recursion, 1-D/2-D arrays | `BillingService.weightedSum` (recursion); `ReportService.hourlyMatrix` (2-D); arrays in every service |
| CO4: Objects That Fail Safely (BTL6) | OOP, encapsulation, exceptions | `MenuItem` hierarchy; `Sensor`/`Billable` interfaces; 38 exception classes; no crash on bad input (transcript) |
| CO5: Strings, Library Algorithms & Files (BTL4) | Strings, searching, sorting, file I/O | `BillingService.parseBillId`; `Arrays.binarySearch`; `Order.compareTo`; `FileStore` |
| CO6: Collections & Streams (BTL6) | Collections, lambdas, streams | `ReportService` (TreeMap, groupingBy, reduce); `FeedbackService` (HashSet); `MenuService` (Predicate, Optional) |
| Checklist CO1: From Problem to Program | | IPO tables and flowcharts in `docs/ALGORITHMS.md` |
| Checklist CO2: Understanding loops and conditions | | Viva guide sections 5–6 |
| Checklist CO3: Applying Methods | | Overloaded `pay` and `readInt`; one `main()` per module |
| Checklist CO4: Exception Handling | | Exception table in `docs/MODULE_AUDIT.md`; checked vs unchecked |
| Checklist CO5: Initialization with Constructors and Polymorphism | | Overloaded constructors (`Customer`, `MenuItem`); `Sensor[]` / `MenuItem[]` polymorphism |
| Checklist CO6: Project Quality, GitHub Repository, Deployment | | README, docs, tests, commit history, run scripts, VS Code settings |
