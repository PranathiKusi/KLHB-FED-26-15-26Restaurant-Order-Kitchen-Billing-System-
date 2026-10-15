# Test Plan and Actual Results

## How to run

```cmd
cd src
javac *.java
java DineSyncTests
```

The runner is plain Java (no JUnit, no downloads). Each check prints `PASS` or `FAIL`; the last lines give the totals. The program exits with code 1 if anything fails.

## Test plan (what is covered)

| Section | What is tested |
|---|---|
| [M01] | valid login for all 4 roles; wrong role letter; bad ID length/letters; unknown ID; role outside 1-4 (unchecked exception); wrong PIN; lock on the 3rd wrong PIN; locked account refuses the correct PIN; only Admin can unlock; counter reset |
| [M02] | add dish #14; duplicate (case-insensitive); price 5 / 2000.01 rejected, 10 / 2000 accepted; name length; prep time; beverage-category rule; non-admin blocked; re-price; unavailable hidden; search; sort; Optional lookup; menu full at 30 |
| [M03] | invalid mobile (starts 5, 9 digits, letters); invalid names; new profile visit #1; duplicate mobile; loyalty pattern over 14 visits; veg-only + favourites-first menu; favourites text; 101st customer |
| [M04] | veg-only rule; quantity 15 / 0; missing dish; full order #2001 = 6 items, Rs.920; merge of repeated dish; kitchen enqueue; dish counts; table rules; role rule; takeaway; 20-item limit; 10-per-dish after merge; empty order; note length; spice level; unavailable dish |
| [M05] | urgent-then-oldest ordering; ETA 24; order 2010 not found; PLACED->SERVED rejected; role rules; chef validation/assignment; READY -> token 101 + display; no reversing; SERVED leaves queue |
| [M11] | '1A7', '1234', blank rejected; '7' -> 007; codes 0x30 0x7E 0x70; '8' lights all segments; style 4 rejected |
| [M06] | billing before SERVED; 920 / 92 / 20.70 / 20.70 / 869.40; cash 800 -> 69.40 due; failed payment leaves table busy; split 0 and 9; UPI 11 digits / letters; role rule; cash 900 split 2 -> 30.60 & 434.70, ID 2001-9; table released 0x4A; double billing; UPI and card payments |
| [M12] | recursion; 2001-9 valid; 2001-8; 2010-9 swapped; 2010-0 not found; 2001/9 and other formats |
| [M07] | rating 6; dish rating 0; wrong rating count; 101-char comment; bad check digit; 4.5 from 215; duplicate feedback; dish average; distribution |
| [M09] | 0x4A binary/hex; suggest T6; capacity; occupied; table 9; party 9; 0x4A -> 0x4E -> 0x4A; release free table; full house; masks |
| [M10] | limits 120 / 29 / 1001 rejected; valid limits; scan settings 0 / 51 / interval 11; alert at 58.4 caught and scanning continues; all-normal scan = 4 x OK; alert log by date; Sensor[] polymorphism; random range |
| [M13] | PF 1.4 / 0; V, I, hours, name ranges; Tandoor 11.73 kWh; 4 appliances 26.70 kWh / Rs.173.60 / Tandoor highest; tariff boundary at 20 units; 16th appliance |
| [M08] | invalid dates (31-09, 29-02-2026, letters, future, format, month 13); leap day 2024; empty reports; revenue = sum of bills; unpaid order excluded; top dishes; top N 0 / 11; hourly matrix totals; staff report; selection sort |
| [CO5] | CSV with comments + 3 malformed records; missing file; write/read/stream-count; audit append; real data/menu.csv loads 13 |
| [INTEGRATION] | full document scenario across all 13 modules, including a background sensor alert during the order, report totals and the audit log |
| [CURRICULUM] | overriding, runtime binding, Comparable, inheritance, checked vs unchecked, constructor overloading, String methods, interface reference, encapsulation, leap-year logic |

## Other executed evidence

* `demo/demo-transcript.txt`: full scripted run of `java Main --demo --fast` (input in `demo/demo-input.txt`), from admin adding Mirchi Bajji to the final reports. It reproduces every number in the document's end-to-end scenario.
* Each module's standalone `main()` (`java AuthService`, `java TableRegister`, ...) was run and finished without errors.
* Mutation check: deliberately breaking the table mask and the kitchen priority rule in a copy of the code made 5 tests fail, which shows the tests actually catch mistakes.
* Compiled with `javac -Xlint:all --release 17` with **no warnings**.

> Sensor readings are random, so lines from `[Background sensor check]` in the transcript can differ between runs. Tests that depend on sensor values force the readings first.

## Actual output of `java DineSyncTests`

```

[M01] Login & Access Control
  PASS T1 W104 + correct PIN opens Waiter
  PASS valid Chef / Cashier / Admin logins
  PASS T2 C104 while role = Waiter -> InvalidStaffIdException: Waiter IDs start with 'W'
  PASS ID wrong length W10 -> InvalidStaffIdException: Staff ID must be 4 characters, e.g. W104
  PASS ID with letters W1X4 -> InvalidStaffIdException: characters 2-4 of a Staff ID must be digits
  PASS unknown ID W999 -> InvalidStaffIdException: no Waiter with ID W999
  PASS role 5 is outside 1-4 (unchecked) -> InvalidMenuChoiceException: Choice 5 is not valid; enter 1-4
  PASS wrong PIN -> InvalidPinException: Wrong PIN (2 attempts left)
  PASS 2nd wrong PIN (letters) -> InvalidPinException: PIN must be exactly 4 digits (1 attempt left)
  PASS T3 3rd wrong PIN locks -> AccountLockedException: 3 wrong attempts - W105 is now locked
  PASS locked account refuses even the correct PIN -> AccountLockedException: W105 is locked after 3 wrong PINs - ask the Admin to unlock it
  PASS waiter cannot unlock -> UnauthorizedAccessException: Waiter Kiran cannot do this; only Admin can
  PASS admin unlocks W105, then login works
  PASS successful login resets the failure counter

[M02] Menu Management
  PASS T1 Mirchi Bajji Rs.120 stored as item #14
  PASS T2 'paneer tikka' duplicate -> DuplicateDishException: "Paneer Tikka" exists
  PASS T3 price 5 -> InvalidPriceException: price must be Rs.10-Rs.2000
  PASS price 2000.01 -> InvalidPriceException: price must be Rs.10-Rs.2000
  PASS boundary prices 10 and 2000 accepted
  PASS name of 2 characters -> InvalidDishException: dish name must be 3-25 characters
  PASS prep time 61 -> InvalidDishException: preparation time must be 1-60 minutes
  PASS Beverage type with category Starters -> InvalidDishException: Beverage type must use category 5 Beverages (and only beverages)
  PASS waiter cannot add dishes -> UnauthorizedAccessException: Waiter Kiran cannot do this; only Admin can
  PASS re-price Veg Biryani to 260
  PASS chef cannot re-price -> UnauthorizedAccessException: Chef Meena cannot do this; only Admin can
  PASS unavailable dish hidden from the order menu
  PASS search('biryani') finds 2 dishes
  PASS search(category 5) finds the beverages
  PASS sortedByPrice starts with Papad (10)
  PASS findByName is case-insensitive (Optional)
  PASS menu fills to 30 then MenuFullException

[M03] Customer Profile & Favourites
  PASS T1 5123456780 starts with 5 -> InvalidPhoneNumberException: must start 6-9
  PASS 9 digits -> InvalidPhoneNumberException: mobile number must have 10 digits (you entered 9)
  PASS letters in number -> InvalidPhoneNumberException: digits only
  PASS name with digits -> InvalidNameException: name may contain letters and spaces only ('2' found)
  PASS name of 1 letter -> InvalidNameException: name must be 2-30 characters
  PASS T2 new number -> profile created, visit #1
  PASS duplicate mobile -> InvalidPhoneNumberException: 9000000001 is already registered
  PASS loyalty on visits 7 and 14 only
  PASS T3 Priya visit #14 -> loyalty, veg-only menu, favourites first
  PASS favourites text
  PASS 101st customer -> CustomerLimitException

[M04] Order Taking
  PASS T1 veg-only customer picks Chicken 65 -> ItemUnavailableException: Chicken 65 is non-veg (Veg only customer)
  PASS T2 quantity 15 -> InvalidQuantityException: qty must be 1-10 (you entered 15)
  PASS quantity 0 -> InvalidQuantityException: qty must be 1-10 (you entered 0)
  PASS dish 99 does not exist -> ItemUnavailableException: dish no. 99 does not exist (1-14)
  PASS T3 full valid order -> #2001, 6 items, Rs.920.00
  PASS order status PLACED and in the kitchen queue
  PASS customer dish counts updated (Veg Biryani 2)
  PASS second order on the same unpaid table -> InvalidTableException: T6 already has an unpaid order
  PASS dine-in on a table that was never allocated -> InvalidTableException: allocate T1 first (M09)
  PASS chef cannot take orders -> UnauthorizedAccessException: Chef Meena cannot do this; only Waiter can
  PASS takeaway order has no table
  PASS 20-item limit: 21st item -> OrderLimitExceededException
  PASS merged quantity over 10 per dish -> InvalidQuantityException: Veg Spring Roll would become x11; max 10 per dish
  PASS empty order cannot be placed -> InvalidInputException: order is empty - add at least one dish
  PASS kitchen note of 61 characters -> InvalidInputException: kitchen note is 61 characters; max 60
  PASS spice level 4 -> InvalidInputException: spice level must be 1 Mild, 2 Medium or 3 Hot
  PASS unavailable dish is rejected

[M05] Kitchen Queue
  PASS queue: urgent #2002, urgent #2003, then normal #2001
  PASS T1 #2001 with 2 orders ahead -> ETA 24 min
  PASS T2 order 2010 -> OrderNotFoundException: no active order #2010
  PASS T3 PLACED -> SERVED directly -> InvalidStatusChangeException: cannot change #2001 from PLACED to SERVED; next allowed stage is PREPARING
  PASS waiter cannot start cooking -> UnauthorizedAccessException: Waiter Kiran cannot do this; only Chef can
  PASS assign 'Kiran' (a waiter) as chef -> InvalidChefException: 'Kiran' is not a chef
  PASS assign chef Arjun
  PASS PLACED -> PREPARING -> READY gives token 101 and display
  PASS reverse READY -> PREPARING -> InvalidStatusChangeException: cannot change #2001 from READY to PREPARING; next allowed stage is SERVED
  PASS chef cannot mark SERVED -> UnauthorizedAccessException: Chef Meena cannot do this; only Waiter can
  PASS waiter marks SERVED; order leaves the queue
  PASS READY order ETA is 0, cooking order ETA uses its own prep

[M11] Seven-Segment Token Display
  PASS T1 '1A7' -> InvalidTokenException: digits only (found 'A')
  PASS '1234' has 4 digits -> InvalidTokenException: at most 3 digits
  PASS blank token -> InvalidTokenException: token is empty
  PASS T2 '7' displayed as 007
  PASS T3 '107' codes 0x30 0x7E 0x70
  PASS ASCII '8' lights all 7 segments
  PASS style 4 -> InvalidInputException: display style must be 1-3

[M06] Billing & Payment
  PASS billing before SERVED -> InvalidStatusChangeException: order #2001 is PLACED; billing is allowed only after SERVED
  PASS subtotal 920.00, discount 92.00
  PASS CGST 20.70, SGST 20.70, total 869.40
  PASS T1 cash Rs.800 -> 69.40 due -> InsufficientPaymentException: Rs.69.40 due
  PASS failed payment keeps T6 busy and order SERVED
  PASS T3 split 0 -> InvalidSplitException: split count must be 1-8 (you entered 0)
  PASS split 9 -> InvalidSplitException: split count must be 1-8 (you entered 9)
  PASS UPI reference with 11 digits -> InvalidPaymentException: UPI reference must be exactly 12 digits
  PASS UPI reference with letters -> InvalidPaymentException: UPI reference must be exactly 12 digits
  PASS waiter cannot take payment -> UnauthorizedAccessException: Waiter Kiran cannot do this; only Cashier can
  PASS T2 cash Rs.900, split 2 -> change 30.60, 434.70 each, bill 2001-9
  PASS order BILLED and T6 released (0x4E -> 0x4A)
  PASS billing the same order twice -> InvalidStatusChangeException: order #2001 is already billed (2001-9)
  PASS UPI and card payments for takeaway orders
  PASS receipt shows grand total line

[M12] Bill Checksum Security
  PASS recursive weightedSum(2001,1) = 9
  PASS check digit of 2010 is 0
  PASS T1 2001-9 valid
  PASS T1 2001-8 -> InvalidBillException: check digit should be 9 (typing error or swapped digits?)
  PASS T2 2010-9 swapped digits (expected 0) -> InvalidBillException: check digit should be 0 (typing error or swapped digits?)
  PASS T3 2010-0 checksum ok but no bill -> BillNotFoundException: checksum is valid but bill 2010-0 does not exist
  PASS T3 2001/9 wrong format -> InvalidBillFormatException: use the format NNNN-D, e.g. 2001-9 (you entered 2001/9)
  PASS abcd-1 letters -> InvalidBillFormatException: bill ID must be digits: NNNN-D, e.g. 2001-9
  PASS 2001-10 two check digits -> InvalidBillFormatException: use the format NNNN-D, e.g. 2001-9 (you entered 2001-10)

[M07] Feedback & Ratings
  PASS T1 rating 6 -> InvalidRatingException: must be 1-5 (you entered 6)
  PASS dish rating 0 -> InvalidRatingException: must be 1-5 (you entered 0)
  PASS wrong number of dish ratings -> InvalidRatingException: expected 4 dish ratings, got 2
  PASS comment of 101 characters -> InvalidInputException: comment is 101 characters; max 100
  PASS bill ID with a bad check digit -> InvalidBillException: check digit should be 9 (typing error or swapped digits?)
  PASS T3 valid ratings -> average 4.5 (215 reviews)
  PASS T2 second feedback for 2001-9 -> DuplicateFeedbackException: feedback for 2001-9 was already given
  PASS dish average Paneer Tikka = 4.0
  PASS rating distribution {5=1}

[M09] Table Occupancy Register
  PASS 0x4A shown as 0100 1010
  PASS party 5 -> suggested T6
  PASS T1 party 5 at T3 -> TableCapacityException: T3 has 4 seats; party is 5
  PASS T2 T5 is busy -> TableOccupiedException: T5 is busy
  PASS table 9 -> InvalidTableException: table must be 1-8 (you entered 9)
  PASS party of 9 -> InvalidInputException: party size must be 1-8 guests
  PASS T3 T6 then release: 0x4A -> 0x4E -> 0x4A
  PASS release a free table -> InvalidTableException: T1 is already free
  PASS suggest returns 0 when nothing fits
  PASS mask(1) = 128 (MSB), mask(8) = 1 (LSB)

[M10] Kitchen Sensor Monitoring
  PASS T1 temperature limit 120 -> InvalidThresholdException: Temperature limit must be 30-80 C only
  PASS temperature limit 29 -> InvalidThresholdException: Temperature limit must be 30-80 C only
  PASS gas limit 1001 -> InvalidThresholdException: LPG Gas limit must be 100-1000 ppm only
  PASS limits 55 C and 500 ppm accepted
  PASS 0 scans -> InvalidThresholdException: number of scans must be 1-50
  PASS 51 scans -> InvalidThresholdException: number of scans must be 1-50
  PASS interval 11 s -> InvalidThresholdException: scan interval must be 1-10 seconds
  PASS T2 58.4 C with limit 55 -> alert caught, scanning continues
  PASS T3 all readings normal -> 4 x OK, no alerts
  PASS alert log has 1 entry for 01-01-2026
  PASS Sensor[] holds 4 different classes (polymorphism)
  PASS random readings stay inside their simulated ranges

[M13] Kitchen Energy Meter
  PASS T1 PF 1.4 -> InvalidReadingException: 0 < PF <= 1
  PASS PF 0 -> InvalidReadingException: 0 < PF <= 1
  PASS voltage 99 -> InvalidReadingException: voltage must be 100-250 V
  PASS current 25 -> InvalidReadingException: current must be 0.1-20 A
  PASS hours 25 -> InvalidReadingException: hours per day must be 0-24
  PASS name 'X' -> InvalidReadingException: appliance name must be 2-20 characters
  PASS T2 230 V x 8.5 A x 1.0 -> 1.955 kW, 6 h = 11.73 kWh
  PASS T3 all 4 appliances -> 26.70 kWh, Rs.173.60, Tandoor highest
  PASS tariff boundary: 20 units = 120.00
  PASS tariff boundary: 20.01 units = 120.08
  PASS tariff: 0 units = 0, 10 units = 60
  PASS 16th appliance -> ApplianceLimitException

[M08] Reports & Analytics
  PASS T1 31-09-2026 -> InvalidDateException: September 2026 has 30 days
  PASS 29-02-2026 (not a leap year) -> InvalidDateException: February 2026 has 28 days
  PASS 29-02-2024 is valid (leap year)
  PASS letters in the date -> InvalidDateException: date must contain digits only (For input string: "2a")
  PASS future date -> InvalidDateException: 16-10-2026 is in the future (today is 15-10-2026)
  PASS wrong format 2026-09-26 -> InvalidDateException: use the format dd-mm-yyyy, e.g. 26-09-2026
  PASS month 13 -> InvalidDateException: month must be 01-12
  PASS T2 date with no orders (empty report) -> NoDataException: no billed orders on 16-10-2026
  PASS staff report with no orders -> NoDataException: no orders on 16-10-2026
  PASS T3 summary: revenue equals the sum of bill totals
  PASS unpaid order #2003 excluded from bills
  PASS top dishes: Veg Biryani (3) first
  PASS top N = 0 -> InvalidInputException: top N must be 1-10
  PASS top N = 11 -> InvalidInputException: top N must be 1-10
  PASS hourly 2-D matrix totals match
  PASS staff report: W104 took 3 orders, K301 billed 2
  PASS selection sort topIndices

[CO5] File I/O (FileStore)
  PASS 2 good records loaded, 3 malformed skipped
  PASS missing file handled
  PASS write report, read back, count lines via stream
  PASS audit log appends lines
  PASS repository data/menu.csv loads 13 dishes (when run from the project)

[INTEGRATION] End-to-end scenario (document section 5)
  PASS new dish appears in the veg menu for Priya
  PASS suggested table for 5 is T6
  PASS background sensor alert raised while order continues
  PASS READY triggers the token display
  PASS bill 2001-9, total 869.40, change 30.60, 434.70 each
  PASS table register back to 0x4A
  PASS rating 4.5 from 215 reviews
  PASS daily report has revenue 869.40, 1 order, alerts, energy 173.60, Mirchi Bajji
  PASS audit log recorded the bill

[CURRICULUM] OOP and language features
  PASS overriding: getTypeLabel differs per subclass
  PASS runtime binding: kitchenStation chosen by object type
  PASS Comparable: Arrays.sort orders by name
  PASS inheritance: Beverage IS-A MenuItem, InvalidPinException IS-A Exception
  PASS checked vs unchecked: DineSyncException is not a RuntimeException
  PASS constructor overloading: Customer(phone, name) defaults to Any diet
  PASS toTitleCase uses split/substring/join
  PASS Order implements Billable (interface reference)
  PASS encapsulation: getLines returns a copy
  PASS leap year rule

==================================================
RESULT: 186 passed, 0 failed, 186 total
==================================================
```
