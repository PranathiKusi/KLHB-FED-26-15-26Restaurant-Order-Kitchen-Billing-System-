# Live Demonstration Script (about 6–8 minutes)

## Before the demo
```cmd
cd src
javac *.java
java DineSyncTests        (show "186 passed, 0 failed")
java Main --demo --fast
```
`--demo` loads the labelled starting situation from the project document. `--fast` removes the waiting time between sensor scans.

**Option B (no typing):** run the whole scenario from a file. Output appears instantly, which is good as a backup if typing goes wrong:
```cmd
cd demo
java -cp ..\src Main --demo --fast < demo-input.txt
```
(On Linux/macOS use `../src`.) The expected output is in `demo/demo-transcript.txt`.

## Steps (type exactly what is shown after ▸)

| # | Who | Do this | What the audience sees | Talking point |
|---|---|---|---|---|
| 1 | Admin | `1` ▸ role `4` ▸ `A401` ▸ PIN `9999` | Welcome, Admin Suresh | Role-based menus (M01) |
| 2 | Admin | `1` Add dish ▸ `paneer tikka` | DuplicateDishException | Case-insensitive duplicate check (M02) |
| 3 | | ▸ `Mirchi Bajji` ▸ category `1` ▸ type `1` ▸ price `5` | InvalidPriceException | Price range ₹10–₹2000 |
| 4 | | ▸ price `120` ▸ prep `10` ▸ `y` ▸ then `0` logout | Added #14 Mirchi Bajji | Object of subclass `VegItem` stored in `MenuItem[]` |
| 5 | Waiter | `1` ▸ `1` ▸ `C104` | InvalidStaffIdException: Waiter IDs start with 'W' | String validation with `charAt` |
| 6 | | ▸ `W104` ▸ PIN `1111` ▸ PIN `4821` | Wrong PIN (2 attempts left), then Welcome | Lock after 3 wrong PINs |
| 7 | | `1` ▸ `5123456780` ▸ `9123456780` | InvalidPhoneNumberException; Welcome back Priya, visit #14, loyalty 10 %, favourites | M03: `14 % 7 == 0` |
| 8 | | `2` ▸ party `5` ▸ table `3` ▸ `5` ▸ `6` | Register 0100 1010 = 0x4A, suggested T6; capacity error; busy error; `reg \|= 1 << 2` → 0x4E | M09 bitwise register |
| 9 | | `4` ▸ `1` (dine-in) ▸ dish `3` qty `2` ▸ dish `7` qty `1` ▸ dish `4` qty `15` ▸ dish `4` qty `1` ▸ dish `14` qty `1` ▸ dish `11` qty `2` ▸ `0` ▸ spice `2` ▸ note `Less spicy, no onion in biryani` | Chicken 65 blocked (veg only); qty 15 rejected; Order #2001 → kitchen · 6 items · ₹920 | M04 validation; the background sensor check line may appear |
| 10 | | `0` logout | | |
| 11 | Chef | `1` ▸ `2` ▸ `C201` ▸ `2468` ▸ `1` view queue | Kitchen queue with ETA | M05 |
| 12 | | `5` ▸ `58.4` then `3` ▸ scans `3` ▸ interval `2` | Temperature 58.4 °C ALERT → exhaust fan ON; scanning continues | M10: exception caught, program keeps running |
| 13 | | `2` ▸ order `2010` ▸ `2001` ▸ action `1`; again `2` ▸ `2001` ▸ `1` | OrderNotFoundException; PREPARING; READY + token 101 drawn + codes | M05 → M11 |
| 14 | | `0` logout. Waiter `W104`/`4821` ▸ `5` ▸ `2001` ▸ `0` | #2001 → SERVED | Strict status order |
| 15 | Cashier | `1` ▸ `3` ▸ `K301` ▸ `3690` ▸ `1` ▸ order `2001` | Bill preview 920 − 92 + 20.70 + 20.70 = ₹869.40 | M06 calculation |
| 16 | | mode `1` ▸ received `800` ▸ `900` ▸ split `0` ▸ `2` | ₹69.40 due; split error; receipt, change ₹30.60, ₹434.70 each, bill ID 2001-9, T6 released → 0x4A | M06 + M12 + M09 |
| 17 | | `2` ▸ `2001/9` ▸ `2010-9` ▸ `2001-9` ▸ blank ▸ `0` | format error; "check digit should be 0"; Genuine bill | M12 recursive check digit |
| 18 | Waiter | login ▸ `6` ▸ `2001-9` ▸ overall `6` ▸ `5` ▸ dishes `5 4 5 4` ▸ comment | InvalidRatingException; Thank you, Priya! 4.5★ (215) | M07 |
| 19 | Admin | login ▸ `7` ▸ `1` ▸ `Induction` ▸ `230` ▸ `9` ▸ PF `1.4` ▸ `0.95` ▸ `4` ▸ `2` ▸ `0` | PF error; energy report 26.70 kWh, ₹173.60, Tandoor highest | M13 slab tariff |
| 20 | | `5` ▸ date `31-09-2026` ▸ blank (today) ▸ type `1` ▸ export `y` | InvalidDateException; daily summary; file saved | M08 + file I/O |
| 21 | | `10` | Audit log read back from the file | File reading + stream count |
| 22 | anyone | main menu `3` | Java concepts lab | Curriculum demonstrations |

**Robustness moment:** at any number prompt, type `abc`. The program says `'abc' is not a whole number` and asks again. It never crashes.

## Screenshots to take for the presentation
1. Steps 7–8 (customer + table register)
2. Step 9 (order with errors)
3. Step 13 (token display)
4. Step 16 (receipt)
5. Step 20 (daily report)
6. `java DineSyncTests` final lines
