# Design Decisions, Document Conflicts and Assumptions

This file lists every place where the source documents (the DineSync project documentation, the PSP Java handout, the faculty review form, the master prompt, and the team's own instructions) **disagree or say nothing**, and what was decided. Nothing here is presented as a documented requirement unless it is one.

## A. Conflicts between documents

| # | Conflict | Decision | Why |
|---|---|---|---|
| D1 | The DineSync document and the master prompt describe Java **packages** (`ui`, `model`, `service`, `monitor`, `exception`). The team later asked for **every `.java` file to run from Notepad + cmd, VS Code, and be uploaded straight into `src/`**. | All classes are in **one flat `src/` folder with no `package` line**. The five layers are kept as a naming convention and are listed in the README. | With packages, compiling from cmd needs `javac -d out` and a folder tree, which is hard for a first-year student to do from Notepad. Flat files compile with `javac *.java` and run with `java Main`. The handout's "packages" topic is still covered by importing `java.util`, `java.nio.file` and so on, and is marked *Partially implemented* in the matrix. |
| D2 | Document section 2.3 places `Main` in `ui`; section 2.4 places `Main.java` at the top level. | `Main.java` sits with all the other files (top level). | Follows D1. |
| D3 | Document: "JDK 17 or later". Handout: "JDK 21 (or latest LTS)". | Code uses only Java 17 features. It was compiled with `javac --release 17` on JDK 21 and tested. | Runs on both. |
| D4 | Handout lists **JUnit 5** and **Maven** as industry tools. The master prompt says no third-party dependencies or frameworks without approval. | Plain-Java test runner `DineSyncTests.java` (186 checks) and no build tool. | Zero setup; works with `javac *.java`. Can be moved to JUnit 5 later. |
| D5 | Document M01 lists the **PIN as `int`**. | PIN is read and stored as a 4-character `String` of digits. | An `int` would turn the PIN `0427` into `427`. "Exactly 4 digits" can only be checked on the text. |
| D6 | M09 sample run: for a party of 5, **T5 → `TableOccupiedException`**, yet T5 has only 4 seats (capacity would also fail). | `allocate()` checks **busy first, then capacity**. | This reproduces the document's sample exactly (T3 → capacity, T5 → busy). |

## B. Worked examples that were checked

| Example in the document | Check | Result |
|---|---|---|
| Bill: 920 − 92 = 828; 2.5% of 828 = 20.70 (×2); total 869.40; change from 900 = 30.60; split 2 = 434.70 | Recomputed | ✔ consistent; reproduced exactly |
| Check digit 2001 → 1×1 + 0×2 + 0×3 + 2×4 = 9 → `2001-9`; `2010` → 10 → `0` | Recomputed | ✔ consistent |
| Table register 0x4A = 0100 1010 → T2, T5, T7 busy; T6 mask = 1 << 2 → 0x4E (78) | Recomputed | ✔ consistent |
| ETA = 20 + 2 × 2 = 24 min | Formula implemented | ✔ when two orders are ahead (the demo makes #2002 and #2003 urgent) |
| Energy: Tandoor 230 × 8.5 × 1.0 ÷ 1000 = **1.955 kW** (document shows 1.96 after rounding) × 6 h = 11.73 kWh ✔; Induction 1.9665 kW × 4 = 7.866 → 7.87 ✔ | Recomputed | ✔ |
| **Energy total 26.70 kWh → ₹173.60.** The unrounded total is 26.6984 kWh, which gives ₹173.59. | **Small inconsistency.** | **Decision:** round the total units to 2 decimals (as a meter displays them) *before* applying the tariff: 26.70 → 20 × 6 + 6.70 × 8 = **₹173.60**. Documented in `EnergyMeter`. |
| Fridge 5.63 kWh and Fan 1.47 kWh: the document does not give their V, I, PF and hours. | **Missing data.** | Assumed inputs that reproduce the totals: Fridge 230 V, 1.2 A, PF 0.85, 24 h → 5.63; Fan 230 V, 0.4 A, PF 0.8, 20 h → 1.47. Labelled as assumed in `DemoData` and `EnergyMeter.main`. |
| **Feedback: "(old total + 5) ÷ (count + 1) → 4.5 from 215 reviews"** | (T + 5) ÷ 215 = 4.5 needs T = 962.5, which is impossible with whole-number ratings. | **Inconsistency.** The 4.5 must be rounded. Demo baseline: **214 earlier reviews totalling 961** (average 4.49). After a 5★ review: 966 ÷ 215 = 4.493 → shown as **4.5**. The baseline is a labelled fixture. |
| Report "Revenue ₹31,240 · 78 orders · 4 top dishes". | Historical numbers, not reproducible without making up 78 orders. | **Not reproduced.** Reports use only real transactions from the current run. The master prompt forbids fabricating usage data. |
| **Token 107** for order #2001. | The document never says how a token number is chosen. | Tokens are issued **in sequence from 101** as orders become READY (#2001 gets 101 in the demo). Token 107 and its codes 0x30 0x7E 0x70 are shown with the Token Display tool and tested in `[M11]`. |

## C. Rules the document leaves open (assumptions)

| Topic | Assumption |
|---|---|
| "Max 20 items per order" | Means total **quantity** (the document's own order "6 items" = 2 + 1 + 1 + 2). The limit of 10 per dish applies after merging repeated selections. |
| Dish categories | 1 Starters, 2 Main Course, 3 Breads, 4 Desserts, 5 Beverages (the document only names 1 and 5). Type *Beverage* must use category 5, and category 5 must be a Beverage. |
| Dish name characters | Letters, digits, spaces, `-`, `'` and `&` (digits are needed for "Chicken 65"). Names are stored in Title Case. |
| Customer name | Letters and spaces only (as documented), stored in Title Case. |
| When a visit is counted | When the waiter identifies the customer (M03 step 3). Loyalty is captured on the order at that time. |
| Who sets which status | PLACED → PREPARING → READY by a **Chef**; READY → SERVED by a **Waiter** (document section 2.6). The chef who starts cooking is assigned automatically if no chef was assigned. |
| "Orders ahead" in ETA | Active orders ahead in priority order that are still PLACED or PREPARING. |
| Smoke and humidity limits | Not given. Smoke 10–50 % (default 30), humidity 40–90 % (default 80). Temperature 30–80 °C and gas 100–1000 ppm follow the document. |
| Sensor alerts "running alongside" orders | Threads are not in the syllabus. After every order is placed, a **one-pass background check** runs and prints only alerts, and the order continues. Full multi-scan sessions run from the Chef/Admin menu with `Thread.sleep` as documented. |
| `Thread.sleep` / `InterruptedException` | Required by the document (M10), though threads are not in the handout. Used only for the pause between scans. |
| Card payment | Simulated approval. No card data is collected. |
| Split bill | Per-person amount = total ÷ n, rounded to paise (869.40 ÷ 2 = 434.70). |
| Releasing a table without a bill | A waiter can release a table only if it has no unpaid order (party left before ordering). |
| Locked accounts | Stay locked until an **Admin** unlocks them (menu option 8). |
| Report dates | Orders are stamped with the computer's date. `java.time.LocalDate/LocalTime` is used **only** to read the clock; date validation itself is hand-written with `split()` and `parseInt()`. |
| Energy cost in reports | The meter holds today's appliances, so energy cost appears only in today's report. |
| Starting menu | Read from `data/menu.csv` (file I/O with malformed-record handling). If the file is missing, the built-in 13-dish menu is used. Changes made while running are not written back. |

## D. Out-of-syllabus items avoided

No GUI, database, network, threads (other than the documented `Thread.sleep`), enums, records, generics declared by us (only *used*), anonymous classes, or third-party libraries. Order statuses use `int` constants instead of an `enum`, because enums are not listed in the handout.
