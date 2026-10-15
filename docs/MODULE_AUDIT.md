# 13-Module Audit and Final Verification Matrix

Every rule from the DineSync project document was checked against the code and the tests.
Statuses: **Implemented and tested** · **Implemented but not fully tested** · **Partially implemented** · **Not implemented**.
"Test" refers to a section of `DineSyncTests` (see `docs/TEST_RESULTS.md`). "Transcript" refers to `demo/demo-transcript.txt`.

## Cluster A: Identity, Menu, Customers & Tables

### M01 Login & Access Control: `Staff`, `AuthService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Roles 1 Waiter, 2 Chef, 3 Cashier, 4 Admin | `Staff` constants, `ConsoleUI.login` | [M01] all roles | Implemented and tested |
| Role outside 1–4 → `InvalidMenuChoiceException` | `AuthService.validateStaffId`, `InputHelper.readInt` | [M01] role 5 | Implemented and tested |
| Staff ID = role letter W/C/K/A + 3 digits, letter matches role | `AuthService.validateStaffId` | [M01] T2 C104 | Implemented and tested |
| Exactly 4-digit PIN | `AuthService.login` | [M01] letters in PIN | Implemented and tested |
| Lock after 3 wrong PINs (`AccountLockedException`) | `Staff.recordFailure` | [M01] T3 | Implemented and tested |
| Role-based permissions (waiter cannot bill, chef cannot change prices) | `AuthService.requireRole` used by every service | [M02], [M04], [M05], [M06] role tests | Implemented and tested |
| Exceptions: InvalidStaffIdException, InvalidPinException, AccountLockedException | exception classes | [M01] | Implemented and tested |

### M02 Menu Management: `MenuItem`, `VegItem`, `NonVegItem`, `Beverage`, `MenuService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Abstract `MenuItem` + 3 subclasses created by type (polymorphism) | `MenuService.create` | [M02] T1 instanceof VegItem | Implemented and tested |
| Capacity 30 → `MenuFullException` | `MenuService.addDish` | [M02] menu full | Implemented and tested |
| Name 3–25 chars, case-insensitive unique → `DuplicateDishException` | `validateName`, `findByName` | [M02] T2 | Implemented and tested |
| Category 1–5, type 1–3 | `addDish` | [M02] beverage rule | Implemented and tested |
| Price ₹10–₹2000 → `InvalidPriceException` | `validatePrice` | [M02] T3, boundaries | Implemented and tested |
| Prep 1–60 min | `addDish` | [M02] prep 61 | Implemented and tested |
| Add, re-price, mark unavailable; unavailable hidden from M04 | `reprice`, `setAvailability`, `menuFor` | [M02], [M04] | Implemented and tested |
| New dish gets the next item number (#14) | `addDish` | [M02] T1; transcript | Implemented and tested |

### M03 Customer Profile & Favourites: `Customer`, `CustomerService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Mobile as `String`, 10 digits, starts 6–9 | `validatePhone` | [M03] T1 | Implemented and tested |
| Name letters and spaces, 2–30 | `validateName` | [M03] | Implemented and tested |
| Diet 1 Any / 2 Veg only | `register` | [M03] T3 | Implemented and tested |
| Array capacity 100 → `CustomerLimitException` | `register` | [M03] 101st | Implemented and tested |
| Visit count; 10 % loyalty when visits % 7 == 0 | `Customer.checkIn` | [M03] pattern; [M06] discount | Implemented and tested |
| Per-dish counts, top-3 favourites, favourites first | `Customer.topFavourites`, `MenuService.menuFor` | [M03] T3 | Implemented and tested |
| Veg-only filtering | `MenuService.menuFor` | [M03] T3 | Implemented and tested |

### M09 Table Occupancy Register: `TableRegister`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| 8 tables, seats {2,2,4,4,4,6,6,8} | `SEATS` | [M09] | Implemented and tested |
| 8-bit register, mask `1 << (8 − t)` | `mask`, `isOccupied` | [M09] masks | Implemented and tested |
| Binary, hex and decimal display | `describe` | [M09] 0x4A | Implemented and tested |
| Allocate `reg \|= mask`, release `reg &= ~mask` | `allocate`, `release` | [M09] T3 | Implemented and tested |
| Suggest the smallest free table that fits | `suggest` | [M09] T6 | Implemented and tested |
| Exceptions: TableOccupied, TableCapacity, InvalidTable | `allocate` | [M09] T1, T2 | Implemented and tested |
| Released by M06 after payment | `BillingService.complete` | [M06], [INTEGRATION] | Implemented and tested |

## Cluster B: Ordering, Kitchen & Token

### M04 Order Taking: `Order`, `OrderItem`, `OrderService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Dine-in / takeaway | `startOrder` | [M04] takeaway | Implemented and tested |
| Quantity 1–10 → `InvalidQuantityException` | `Order.addItem` | [M04] T2 | Implemented and tested |
| Spice 1–3; note ≤ 60 chars | `placeOrder` | [M04] | Implemented and tested |
| Merge repeated dish | `Order.addItem` | [M04] merge (4 lines) | Implemented and tested |
| Max 20 items → `OrderLimitExceededException` | `Order.addItem` | [M04] 21st item | Implemented and tested |
| Reject unavailable / non-veg for veg-only → `ItemUnavailableException` | `OrderService.addItem` | [M04] T1 | Implemented and tested |
| Subtotal, order ID from 2001, status PLACED | `placeOrder` | [M04] T3 | Implemented and tested |
| Update customer dish counts; enqueue in kitchen | `placeOrder` | [M04] | Implemented and tested |

### M05 Kitchen Queue: `Kitchen`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Urgent first, then oldest | `Order.compareTo`, `getActiveSorted` | [M05] | Implemented and tested |
| ETA = longest prep + 2 × orders ahead | `eta` | [M05] T1 = 24 | Implemented and tested |
| Chef assignment must name a chef → `InvalidChefException` | `assignChef` | [M05] | Implemented and tested |
| Strict PLACED → PREPARING → READY → SERVED → `InvalidStatusChangeException` | `changeStatus` | [M05] T3, reverse | Implemented and tested |
| `OrderNotFoundException` for inactive IDs | `findActive` | [M05] T2 | Implemented and tested |
| READY → token display (M11) + table buzzer (simulated) | `changeStatus` | [M05]; transcript | Implemented and tested |

### M11 Seven-Segment Token Display: `SevenSegment`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| 1–3 digits only → `InvalidTokenException` | `validateToken` | [M11] T1 | Implemented and tested |
| Pad to 3 digits | `padLeft` (recursive) | [M11] T2 | Implemented and tested |
| Codes {0x7E,0x30,0x6D,0x79,0x33,0x5B,0x5F,0x70,0x7F,0x7B} | `CODES` | [M11] T3 | Implemented and tested |
| `String[10][3]` patterns, 7-bit binary, ASCII/Binary/Both | `PATTERNS`, `render` | [M11] | Implemented and tested |

## Cluster C: Billing, Checksum & Feedback

### M06 Billing & Payment: `Bill`, `Billable`, `BillingService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Only after SERVED | `billableOrder` | [M06] | Implemented and tested |
| Subtotal through `Billable` | `calculate(Billable)` | [M06] | Implemented and tested |
| Loyalty 10 %, CGST 2.5 %, SGST 2.5 % | `calculate` | [M06] 869.40 | Implemented and tested |
| Cash ≥ total → `InsufficientPaymentException` with amount due | `checkCash` | [M06] T1 | Implemented and tested |
| UPI 12 digits → `InvalidPaymentException`; card | `pay` overloads | [M06] | Implemented and tested |
| Split 1–8 → `InvalidSplitException`; change and per-person share | `checkSplit`, `Bill` | [M06] T2, T3 | Implemented and tested |
| Bill ID from M12; BILLED; table released only after success | `complete` | [M06] | Implemented and tested |

### M12 Bill Checksum Security: `BillingService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Format NNNN-D via `indexOf`/`substring`; `NumberFormatException` → `InvalidBillFormatException` | `parseBillId` | [M12] T3 | Implemented and tested |
| Weighted sum right to left, weights 1–4; `% 10` | `weightedSum`, `checkDigit` | [M12] | Implemented and tested |
| Wrong digit → `InvalidBillException` (shows expected digit) | `verify` | [M12] T1, T2 | Implemented and tested |
| Valid but missing → `BillNotFoundException` | `verify` (binary search) | [M12] T3 | Implemented and tested |

### M07 Feedback & Ratings: `Feedback`, `FeedbackService`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Verify bill with M12 | `verifyForFeedback` | [M07] | Implemented and tested |
| One feedback per bill → `DuplicateFeedbackException` | `HashSet` check | [M07] T2 | Implemented and tested |
| Overall and per-dish ratings 1–5 → `InvalidRatingException` | `validateRating` | [M07] T1 | Implemented and tested |
| Comment ≤ 100 chars | `submit` | [M07] | Implemented and tested |
| Running averages (restaurant and per dish) | `averageRating`, `dishAverage` | [M07] T3 = 4.5 (215) | Implemented and tested |

## Cluster D: Monitoring, Energy & Analytics

### M10 Kitchen Sensor Monitoring: `Sensor`, `AbstractSensor`, 4 sensors, `KitchenMonitor`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| 4 sensors in one `Sensor[]` | `KitchenMonitor.sensors` | [M10] | Implemented and tested |
| `read()` with `Math.random()` in realistic ranges | `AbstractSensor.read` | [M10] range | Implemented and tested |
| Temperature limit 30–80, gas 100–1000 → `InvalidThresholdException` | `setLimit` | [M10] T1 | Implemented and tested |
| Scans 1–50, interval 1–10 s | `validateScanSettings` | [M10] | Implemented and tested |
| Alert → `SensorAlertException` caught, logged in `String[] alertLog`, scanning continues, automatic action | `scanOnce`, `logAlert` | [M10] T2 | Implemented and tested |
| `Thread.sleep(interval × 1000)`; `InterruptedException` caught | `runScans` | Code review; normal path executed in transcript (fast mode skips the wait) | Implemented but not fully tested (the interrupt path cannot be triggered from a single-threaded test) |
| Alert count sent to M08 | `countAlertsOn` | [INTEGRATION] | Implemented and tested |

### M13 Kitchen Energy Meter: `Appliance`, `EnergyMeter`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Up to 15 appliances → `ApplianceLimitException` | `addAppliance` | [M13] | Implemented and tested |
| Name 2–20; V 100–250; I 0.1–20; 0 < PF ≤ 1; hours 0–24 → `InvalidReadingException` | `check…` methods | [M13] | Implemented and tested |
| kW = V × I × PF ÷ 1000; kWh = kW × h | `Appliance` | [M13] T2 | Implemented and tested |
| Slab: 20 units at ₹6, rest at ₹8 | `cost` | [M13] T3, boundary | Implemented and tested |
| Highest consumer | `highest` | [M13] | Implemented and tested |

### M08 Reports & Analytics: `ReportService`, `DateHelper`
| Rule | Where | Evidence | Status |
|---|---|---|---|
| Date dd-mm-yyyy with `split`/`parseInt`, month lengths, leap years, not in the future → `InvalidDateException` | `DateHelper.validate` | [M08] T1 | Implemented and tested |
| Report types: summary, top dishes (N 1–10), hourly, staff | `ReportService` | [M08] | Implemented and tested |
| Revenue = sum of bill totals; order count; ratings; alerts; energy cost | `summary` | [M08] T3; [INTEGRATION] | Implemented and tested |
| `NoDataException` when there are no billed orders | `requireBills` | [M08] T2 | Implemented and tested |

## Exception summary (document section 6) → all present

| Module | Exceptions (all implemented as classes and thrown by the code) |
|---|---|
| M01 | InvalidStaffIdException, InvalidPinException, AccountLockedException (+ InvalidMenuChoiceException, UnauthorizedAccessException) |
| M02 | MenuFullException, DuplicateDishException, InvalidPriceException (+ InvalidDishException) |
| M03 | InvalidPhoneNumberException, InvalidNameException, CustomerLimitException |
| M04 | ItemUnavailableException, InvalidQuantityException, OrderLimitExceededException |
| M05 | OrderNotFoundException, InvalidStatusChangeException, InvalidChefException |
| M06 | InsufficientPaymentException, InvalidSplitException, InvalidPaymentException |
| M07 | InvalidRatingException, DuplicateFeedbackException, InvalidBillException |
| M08 | InvalidDateException, NoDataException, NumberFormatException (built-in, caught) |
| M09 | TableOccupiedException, TableCapacityException, InvalidTableException |
| M10 | SensorAlertException, InvalidThresholdException, InterruptedException (built-in, caught) |
| M11 | InvalidTokenException |
| M12 | InvalidBillFormatException, InvalidBillException, BillNotFoundException |
| M13 | InvalidReadingException, ApplianceLimitException |
| General | InvalidInputException (note/comment length, party size, spice level, style) |

## Document section 7: integration tests

| Test | Result |
|---|---|
| Full visit, outputs match section 5 | ✔ [INTEGRATION]; transcript |
| Veg-only rule uses the M03 preference in M04 | ✔ [M04] T1 |
| Table release: paying #2001 frees T6, register back to 0x4A | ✔ [M06]; transcript |
| Token trigger: READY calls M11 | ✔ [M05], [INTEGRATION] |
| Bill reuse: feedback with 2001-9 verified via M12, accepted once | ✔ [M07] |
| New dish appears in M04 menu and M08 report | ✔ [INTEGRATION] (Mirchi Bajji in menu and summary) |
| Unavailable dish hidden from M04 | ✔ [M02] |
| Background alert during an order, order flow continues | ✔ [INTEGRATION]; transcript |
| Report revenue equals the sum of M06 bills | ✔ [M08] |
| Letters typed at a number prompt → message, no crash | ✔ `InputHelper.readInt` (try it live; shown in `DEMO_SCRIPT.md`) |

---

## Final completion checklist

### Functional completeness
| Item | Status |
|---|---|
| All 13 modules audited against the specification | Implemented and tested |
| All four clusters integrated (shared `Restaurant` object, one copy of each record) | Implemented and tested |
| Complete restaurant workflow (login → report) | Implemented and tested (transcript + [INTEGRATION]) |
| Invalid operations rejected; no fake success messages | Implemented and tested |
| Calculations correct and reconciled | Implemented and tested |

### Curriculum completeness
| Item | Status |
|---|---|
| Handout reviewed (syllabus Modules 1–6, CO table, COIs, sessions 1–30) | Done |
| Every syllabus topic listed in `CURRICULUM_MATRIX.md` | Done |
| Topics implemented | All implemented and tested, except: packages (partial: own code in default package at the team's request); JUnit 5 and Maven (not used, deliberately) |
| Beginner checklist (data types, operators, Scanner, control flow, methods, arrays, inheritance, strings, exceptions, overloading/overriding) | All implemented and tested |
| No unrelated advanced technology | Confirmed (see `DESIGN_DECISIONS.md` section D) |

### Testing
| Item | Status |
|---|---|
| Program compiles (`javac --release 17 *.java`, `-Xlint:all` clean) | Done |
| Tests executed: 186 passed, 0 failed | Done |
| Defects found and fixed during development | See "Defects fixed" below |
| Unverified features identified | `InterruptedException` path in M10 |

### Defects found and fixed during development
1. Unicode symbols (₹ ✔) printed as `?` in consoles that are not UTF-8. **Fix:** `Colors` detects the console encoding and falls back to ASCII; added the `--ascii`/`--unicode` flags.
2. ANSI colour codes would print as junk in the old Windows `cmd`. **Fix:** colours turn on automatically only for VS Code, Windows Terminal and Linux/macOS terminals.
3. A party of 5 at T5 gave a capacity error instead of the document's "T5 is busy". **Fix:** check occupancy before capacity.
4. Non-ASCII characters in source comments could break compilation on JDK 17 for Windows (Cp1252 default). **Fix:** all source files are pure ASCII (symbols use `\u` escapes).
5. A standalone demo used an anonymous class, which is outside the syllabus. **Fix:** replaced it with a real `Order`.
6. Compiler lint warnings (missing `serialVersionUID`) would show in VS Code. **Fix:** added the field to every exception, so the build has zero warnings.

### Documentation and presentation
| Item | Status |
|---|---|
| README complete | Done |
| GitHub-ready structure (`src/`, `data/`, `docs/`, `demo/`, `.gitignore`, `.vscode/`) | Done |
| Curriculum mapping accurate | Done |
| Poster content lists only verified concepts | Done (`POSTER_CONTENT.md`) |
| Presentation content uses genuine output (screenshots still to be taken by the team, placeholders marked) | Done (`PRESENTATION_CONTENT.md`) |
| Viva explanation | Done (`VIVA_GUIDE.md`) |
| Limitations and future scope honest | Done (README) |
