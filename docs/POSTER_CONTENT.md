# Poster Content: DineSync

*(Every Java concept listed here was checked in the source code. See `CURRICULUM_MATRIX.md`.)*

## Title
**DineSync: Smart Restaurant Management System with IoT-Based Kitchen Monitoring**
Project 15 · Money & Commerce · 26SC1101E Problem Solving Using Programming (JAVA) · KL University · 2026–27
Pranathi Kusi (2620040061) · M. Sri Akhil Chowdary (2620040112) · Guide: Dr. K. Sreeram Murthy

## Problem statement
Paper order slips cause slow floor-to-kitchen communication, lost or duplicated orders, wrong discounts and tax, no memory of regular customers, late detection of kitchen hazards, and no daily view of sales or running costs.

## Proposed solution
One Core Java console system with 13 connected modules. An order is entered **once** and flows automatically from table to kitchen to bill to feedback to report, while monitoring modules track tables, kitchen safety and energy use.

## Objectives
- A single connected order flow with role-based access
- Accurate bills: loyalty discount, CGST + SGST, cash, UPI, card, split
- Remember customer favourites and diet preference
- Validate every input; never crash on an error
- Simulated kitchen sensors with alerts and automatic actions
- Daily reports built only from real transactions

## 13 modules in 4 clusters
| A. Identity & Tables | B. Orders & Kitchen | C. Billing & Feedback | D. Monitoring & Analytics |
|---|---|---|---|
| M01 Login & Access | M04 Order Taking | M06 Billing & Payment | M10 Kitchen Sensors |
| M02 Menu Management | M05 Kitchen Queue | M12 Bill Checksum | M13 Energy Meter |
| M03 Customers & Favourites | M11 7-Segment Token | M07 Feedback & Ratings | M08 Reports & Analytics |
| M09 Table Register (8-bit) | | | |

## Architecture (draw as boxes)
`Main → ConsoleUI → Restaurant (one shared object) → Services (Auth, Menu, Customer, Order, Kitchen, Billing, Feedback, Report) → Models (Staff, MenuItem…, Order, Bill) + Monitors (TableRegister, Sensors, SevenSegment, EnergyMeter) → Exceptions (38 classes) + FileStore (menu.csv, audit.log, reports)`

## Workflow
Login → Customer → Table → Order → Kitchen → **READY token** → Served → Bill + check digit → Table released → Feedback → Daily report

## Key results (all produced by the program)
| | |
|---|---|
| Order #2001 | 6 items, ₹920.00 |
| Bill 2001-9 | ₹920 − ₹92 + ₹20.70 + ₹20.70 = **₹869.40** |
| Check digit | 1×1 + 0×2 + 0×3 + 2×4 = 9 |
| Table register | 0100 1010 (0x4A) → 0100 1110 (0x4E) → 0x4A |
| Kitchen ETA | 20 + 2 × 2 = 24 min |
| Energy | 26.70 kWh → 20 × ₹6 + 6.70 × ₹8 = **₹173.60** |
| Rating | 4.5★ (215 reviews, including the demo baseline) |

## Testing evidence
**186 automated checks, 186 passed, 0 failed** (`java DineSyncTests`) · full scripted run recorded · every module also runs on its own (`java TableRegister`, `java BillingService` …) · zero compiler warnings

## Java Concepts Used
- **Fundamentals:** primitive types (`int`, `double`, `char`, `boolean`, `long`), constants, arithmetic/relational/logical/ternary/unary operators, **bitwise** `& | ~ << >>`, casting, `Scanner`, `printf`
- **Control flow:** `if`, nested `if`, if-else-if, `switch` menus, `for`, for-each, `while` (sentinel), `do-while`, `break`, `continue`, nested loops
- **Methods, recursion, arrays:** overloaded methods, **recursive check digit**, 1-D arrays for all records, 2-D `String[10][3]` and `double[24][3]`, linear search, selection sort
- **OOP:** classes and objects, overloaded constructors with `this(...)`, encapsulation, **inheritance** (`MenuItem` → Veg/NonVeg/Beverage; sensors; exceptions), abstract classes, interfaces (`Billable`, `Sensor`, `Comparable`), **overriding and runtime polymorphism**, `static`, `toString`
- **Strings and algorithms:** `charAt`, `substring`, `indexOf`, `split`, `join`, `equals` vs `==`, `StringBuilder`/`StringBuffer`, `Arrays.sort`, `Arrays.binarySearch`, `Comparator`
- **Exceptions and files:** 38 custom exceptions, checked and unchecked, try-catch-finally, multi-catch, `throw`/`throws`, `Files`/`Path`, buffered I/O, append, try-with-resources, malformed-record handling
- **Collections and streams:** `ArrayList`, `HashSet`, `TreeMap`, `Iterator`, generics, autoboxing, lambdas, method references, `Predicate`/`Function`/`Consumer`, `filter`/`map`/`reduce`/`count`, `groupingBy`/`counting`/`joining`, `Optional`, `Files.lines`

## PSP CO1–CO6 mapping (compact)
| CO | Shown by |
|---|---|
| CO1 Problem → Program | IPO + flowcharts; GST and energy formulas; bitwise register; Scanner I/O |
| CO2 Control Flow | Menu `switch`, retry loops, 3-attempt lock, nested loops for the display |
| CO3 Methods, Recursion, Arrays | Recursive check digit; arrays for all records; 2-D hourly report |
| CO4 OOP + Exceptions | MenuItem/Sensor hierarchies; Billable interface; 38 exceptions; no crashes |
| CO5 Strings, Algorithms, Files | ID/date parsing; binary search on bills; sorting; menu CSV, audit log |
| CO6 Collections & Streams | Reports with TreeMap, groupingBy, reduce; HashSet duplicate check |

## Project status
All 13 modules implemented, integrated and tested. Prototype limits: data in memory only, simulated sensors and payments, single console user.

## Future scope
Saved data (files, then a database) → GUI or web screens → multi-terminal kitchen display → QR ordering and a payment gateway → real sensors and display through a microcontroller.
