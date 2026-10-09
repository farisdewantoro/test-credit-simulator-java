# credit_simulator User Guide

This guide takes you from a fresh machine to using every feature of `credit_simulator`, step by step.
All example output below was produced by the application itself.

**Contents**

1. [What you need](#1-what-you-need)
2. [Setup](#2-setup)
3. [Start the app](#3-start-the-app)
4. [Use cases](#4-use-cases)
   1. [Calculate installments with guided questions](#uc1-calculate-installments-with-guided-questions)
   2. [Enter the loan field by field](#uc2-enter-the-loan-field-by-field)
   3. [Fix invalid input and broken rules](#uc3-fix-invalid-input-and-broken-rules)
   4. [Load an existing calculation from the web service](#uc4-load-an-existing-calculation-from-the-web-service)
   5. [Compare loan options with sheets](#uc5-compare-loan-options-with-sheets)
   6. [Run commands from a file](#uc6-run-commands-from-a-file)
   7. [Run with Docker instead of Java](#uc7-run-with-docker-instead-of-java)
5. [Input rules at a glance](#5-input-rules-at-a-glance)
6. [How to read the result](#6-how-to-read-the-result)
7. [Troubleshooting](#7-troubleshooting)
8. [Command reference](#8-command-reference)

---

## 1. What you need

| | Needed for | Notes |
|---|---|---|
| macOS or Linux terminal | everything | Windows users: use WSL or Docker |
| **JDK 17 or newer** | running with `./credit_simulator` | Not needed if you use Docker |
| Internet access | first run only | Downloads Maven and the JSON library to build the app; `load` also needs it |
| Docker | optional | Alternative to installing Java (see [UC7](#uc7-run-with-docker-instead-of-java)) |

You do **not** need to install Maven. The project includes the Maven Wrapper (`./mvnw`).

## 2. Setup

### Step 1: Install Java 17+

Check whether you already have it:

```sh
java -version
```

If this prints `version "17..."` or higher (`21`, `25`, …), skip to step 2. Otherwise install a JDK:

| System | Command |
|---|---|
| macOS (Homebrew) | `brew install openjdk@17`, then follow the `sudo ln -sfn …` hint it prints |
| Ubuntu / Debian | `sudo apt-get update && sudo apt-get install -y openjdk-17-jdk` |
| Fedora / RHEL | `sudo dnf install -y java-17-openjdk-devel` |
| Any (SDKMAN) | `curl -s "https://get.sdkman.io" \| bash`, open a new terminal, run `sdk list java` and install a 17 Temurin build: `sdk install java <17.x.x>-tem` |

Open a new terminal and run `java -version` again to confirm.

> On macOS, `java -version` can print *"Unable to locate a Java Runtime"* even though the `java`
> command exists. That means no JDK is installed yet.

### Step 2: Get the code

From the tarball:

```sh
tar -xzf credit_simulator.tar.gz
cd credit_simulator
```

Or from the Git repository:

```sh
git clone <repository-url> credit_simulator
cd credit_simulator
```

### Step 3: Build and start it for the first time

```sh
./credit_simulator
```

The first run builds the application, which takes about a minute:

```
credit_simulator: building (first run only, this can take a minute)...
Credit Simulator. Type 'show' to list commands or 'exit' to quit.
>
```

Type `exit` to quit. Later runs start immediately. The launcher rebuilds by itself if the source code
changes.

> If you get `Permission denied`, the executable bits were lost while copying. Fix it once with
> `chmod +x credit_simulator bin/credit_simulator mvnw scripts/smoke_test.sh`.

### Step 4 (optional): Check that everything works

```sh
scripts/smoke_test.sh
```

It runs the app the way a user would and ends with `All smoke tests passed.` To run the full unit-test
suite instead: `./mvnw test`.

## 3. Start the app

There are two ways to run it, and both launchers behave the same:

| Mode | Command | Use it when |
|---|---|---|
| Interactive | `./credit_simulator` or `bin/credit_simulator` | You want to type commands and see results immediately |
| File | `./credit_simulator file_inputs.txt` or `bin/credit_simulator file_inputs.txt` | You have prepared the commands in a text file |

In interactive mode, `>` is the prompt. Type `show` to list every command:

```
> show
Available commands:
  show                  List all available commands
  new                   Guided input: asks for every field in order, then calculates
  jenis <Mobil|Motor>   Set the vehicle type
  kondisi <Baru|Bekas>  Set the vehicle condition (new or used)
  tahun <yyyy>          Set the vehicle year (4 digits)
  nominal <amount>      Set the total loan amount in rupiah (max 1000000000)
  tenor <1-6>           Set the loan tenor in years
  dp <amount>           Set the down payment in rupiah (Baru >= 35%, Bekas >= 25% of loan)
  status                Show the active sheet's inputs and its last result
  calculate             Validate the inputs and show the monthly installment for each year
  load                  Load an existing calculation from the web service and calculate it
  save_sheet <name>     Save the active sheet's inputs and result as <name> and make it the active sheet
  switch_sheet <name>   Make <name> the active sheet and show its inputs and result
  list_sheets           List all sheets; '*' marks the active one
  exit                  Quit the application
```

Commands are not case-sensitive (`JENIS MOBIL` works too). To quit, type `exit` or press `Ctrl+D`.

## 4. Use cases

### UC1: Calculate installments with guided questions

**Goal:** get the monthly installments for a loan without remembering any commands.

1. Start the app: `./credit_simulator`
2. Type `new` and press Enter.
3. Answer each question and press Enter after each answer:

```
> new
Jenis Kendaraan (Motor|Mobil): Mobil
Kendaraan (Baru|Bekas): Bekas
Tahun Kendaraan (4 digit): 2022
Jumlah Pinjaman Total (maks 1000000000): 100000000
Tenor Pinjaman (1-6 thn): 3
Jumlah DP: 25000000
Monthly installments for Mobil Bekas 2022, principal Rp. 75,000,000.00:
tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%
```

**Result:** you pay Rp 2,250,000 a month in year 1, Rp 2,432,250 in year 2 and Rp 2,641,423.50 in year 3.

If you mistype an answer, the app explains what's wrong and asks the same question again:

```
Tenor Pinjaman (1-6 thn): 7
Error: Tenor must be between 1 and 6 years. Please try again.
Tenor Pinjaman (1-6 thn): 3
```

### UC2: Enter the loan field by field

**Goal:** set or change single values and check them before calculating. The fields can be entered in
any order.

1. Set each field with its command:

```
> jenis motor
Vehicle type set to Motor
> kondisi baru
Vehicle condition set to Baru
> tahun 2026
Vehicle year set to 2026
> nominal 50000000
Loan amount set to Rp. 50,000,000.00
> tenor 2
Tenor set to 2 thn
> dp 20000000
Down payment set to Rp. 20,000,000.00
```

2. Review what you entered with `status`. Unset fields show `-`:

```
> status
Sheet 'default'
  jenis   : Motor
  kondisi : Baru
  tahun   : 2026
  nominal : Rp. 50,000,000.00
  tenor   : 2 thn
  dp      : Rp. 20,000,000.00
  result  : not calculated
```

3. Calculate:

```
> calculate
Monthly installments for Motor Baru 2026, principal Rp. 30,000,000.00:
tahun 1 : Rp. 1,362,500.00/bln , Suku Bunga : 9%
tahun 2 : Rp. 1,486,487.50/bln , Suku Bunga : 9,1%
```

4. To try a different value, change only that field and run `calculate` again, e.g. `tenor 3` and then
   `calculate`. Changing any field clears the old result, so `status` never shows a result that doesn't
   match the inputs.

### UC3: Fix invalid input and broken rules

**Goal:** understand the error messages and correct the input.

The app checks your input at two moments:

1. **When you set a field**, it checks the format straight away:

```
> jenis truk
Error: Vehicle type must be Mobil or Motor
> tahun 26
Error: Vehicle year must be a 4-digit number, e.g. 2022
> nominal 1.000.000
Error: Loan amount must be a whole number of rupiah written with digits only, e.g. 100000000
> tenor 7
Error: Tenor must be between 1 and 6 years
> jenis
Error: Usage: jenis <Mobil|Motor>
> foo
Error: Unknown command 'foo'. Type 'show' to list commands.
```

2. **When you run `calculate`**, it checks the rules that involve several fields and lists **every**
   problem at once:

```
> jenis mobil
> kondisi baru
> tahun 2020
> nominal 2000000000
> dp 100000000
> calculate
Error: Cannot calculate, 4 problems found:
  - Missing: tenor
  - A new (Baru) vehicle cannot be older than 2025, got 2020
  - Loan amount cannot exceed Rp. 1,000,000,000.00
  - DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 700,000,000.00)
```

Fix the fields one by one and calculate again. Each message tells you the allowed value:

```
> tenor 4
> tahun 2025
> nominal 300000000
> calculate
Error: DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 105,000,000.00)
> dp 105000000
> calculate
Monthly installments for Mobil Baru 2025, principal Rp. 195,000,000.00:
tahun 1 : Rp. 4,387,500.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 4,742,887.50/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 5,150,775.83/bln , Suku Bunga : 8,6%
tahun 4 : Rp. 5,598,893.32/bln , Suku Bunga : 8,7%
```

An error never closes the app. Your other values are kept, so you only fix what's wrong.

> The year limits depend on today's date. In 2026, a `Baru` (new) vehicle must be from 2025 or later, and
> no vehicle can be later than 2027.

### UC4: Load an existing calculation from the web service

**Goal:** fetch a saved calculation from the server and see its result immediately.

1. Type `load`. The app fetches the data, puts it in the current sheet and calculates it:

```
> load
Loaded into sheet 'default': Mobil Baru 2025, loan Rp. 1,000,000,000.00, tenor 6 thn, DP Rp. 500,000,000.00
Monthly installments for Mobil Baru 2025, principal Rp. 500,000,000.00:
tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 8,107,500.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 8,804,745.00/bln , Suku Bunga : 8,6%
tahun 4 : Rp. 9,570,757.82/bln , Suku Bunga : 8,7%
tahun 5 : Rp. 10,451,267.53/bln , Suku Bunga : 9,2%
tahun 6 : Rp. 11,423,235.41/bln , Suku Bunga : 9,3%
```

2. The loaded values are now in the sheet. Check them with `status` or change them like typed input
   ([UC2](#uc2-enter-the-loan-field-by-field)).

**If the loaded data breaks a rule,** it is still loaded so you can fix it, and the problem is shown:

```
> load
Loaded into sheet 'default': Mobil Baru 2020, loan Rp. 1,000,000,000.00, tenor 6 thn, DP Rp. 500,000,000.00
Error: A new (Baru) vehicle cannot be older than 2025, got 2020
> tahun 2026
Vehicle year set to 2026
> calculate
Monthly installments for Mobil Baru 2026, principal Rp. 500,000,000.00:
tahun 1 : Rp. 7,500,000.00/bln , Suku Bunga : 8%
...
```

**If the service can't be reached,** you get a clear error and your current sheet is left unchanged:

```
> load
Error: Could not reach calculation service: connection refused (check the URL and that the service is up)
```

#### Use a different web service (or work offline)

By default `load` calls `https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c`. Mocky links can
expire, and some networks block them. Point the app at another URL with the environment variable
`CREDIT_SIMULATOR_LOAD_URL`.

To try `load` fully offline, serve a JSON file from your own machine:

```sh
# 1. Create the JSON the service would return
mkdir -p /tmp/cs-service && cat > /tmp/cs-service/calculation.json <<'EOF'
{"vehicleType": "Mobil", "vehicleCondition": "Baru", "vehicleYear": 2025,
 "totalLoanAmount": 1000000000, "loanTenure": 6, "downPayment": 500000000}
EOF

# 2. Serve it (leave this running in a second terminal)
cd /tmp/cs-service && python3 -m http.server 8000

# 3. Back in the project, start the app pointing at it, then type: load
CREDIT_SIMULATOR_LOAD_URL=http://localhost:8000/calculation.json ./credit_simulator
```

### UC5: Compare loan options with sheets

**Goal:** keep several calculations side by side, for example the same car over 3 and 5 years.

**How sheets work:**

- The app starts with one sheet, `default`. Commands like `jenis` or `calculate` always work on the
  **active** sheet.
- `save_sheet <name>` works like *Save as*. It copies the active sheet to `<name>` **and switches to the
  copy**, so later edits change `<name>`.
- To branch off a variant, **save under the new name first, then edit**.

Step by step:

1. Calculate the first option (3 years):

```
> jenis mobil
> kondisi bekas
> tahun 2021
> nominal 200000000
> dp 50000000
> tenor 3
> calculate
Monthly installments for Mobil Bekas 2021, principal Rp. 150,000,000.00:
tahun 1 : Rp. 4,500,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 4,864,500.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 5,282,847.00/bln , Suku Bunga : 8,6%
```

2. Keep it as `tenor-3`:

```
> save_sheet tenor-3
Saved sheet 'tenor-3' from 'default'; 'tenor-3' is now the active sheet
```

3. Branch the second option: save a copy as `tenor-5` **first**, then change the tenor:

```
> save_sheet tenor-5
Saved sheet 'tenor-5' from 'tenor-3'; 'tenor-5' is now the active sheet
> tenor 5
Tenor set to 5 thn
> calculate
Monthly installments for Mobil Bekas 2021, principal Rp. 150,000,000.00:
tahun 1 : Rp. 2,700,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 2,918,700.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 3,169,708.20/bln , Suku Bunga : 8,6%
tahun 4 : Rp. 3,445,472.81/bln , Suku Bunga : 8,7%
tahun 5 : Rp. 3,762,456.31/bln , Suku Bunga : 9,2%
```

4. See all sheets. `*` marks the active one:

```
> list_sheets
  default  Mobil/Bekas/2021  Rp. 200,000,000.00  3 thn  ✔ calculated
  tenor-3  Mobil/Bekas/2021  Rp. 200,000,000.00  3 thn  ✔ calculated
* tenor-5  Mobil/Bekas/2021  Rp. 200,000,000.00  5 thn  ✔ calculated
```

5. Switch back to any sheet. Its inputs and result are shown right away:

```
> switch_sheet tenor-3
Switched to sheet 'tenor-3'
Sheet 'tenor-3'
  jenis   : Mobil
  kondisi : Bekas
  tahun   : 2021
  nominal : Rp. 200,000,000.00
  tenor   : 3 thn
  dp      : Rp. 50,000,000.00
  result  :
    tahun 1 : Rp. 4,500,000.00/bln , Suku Bunga : 8%
    tahun 2 : Rp. 4,864,500.00/bln , Suku Bunga : 8,1%
    tahun 3 : Rp. 5,282,847.00/bln , Suku Bunga : 8,6%
```

**Good to know**

- Sheet names: 1–32 characters, using letters, digits, `-` or `_` (no spaces). They are not
  case-sensitive, so `switch_sheet TENOR-3` works.
- Saving under an existing name replaces that sheet: `Overwrote sheet 'a' from 'default'; ...`.
- An unknown name lists what exists: `Error: Sheet 'x' not found. Available sheets: default, a`.
- To start a fresh calculation without touching your saved sheets, run `switch_sheet default` first, or
  use `new`. `new` replaces all fields of the active sheet.
- Sheets last until you quit the app. They are not saved to disk.

### UC6: Run commands from a file

**Goal:** run a prepared list of commands in one go, for example to repeat a calculation or check results
in a script.

1. **Try the included sample.** It runs two calculations and the sheet workflow from UC5:

```sh
./credit_simulator samples/file_inputs.txt
```

Each command is echoed with `>` before its output, so the result reads like a transcript.

2. **Write your own file**, one command per line, exactly as you would type it. Blank lines and lines
   starting with `#` are ignored. Save it as `my_loan.txt`:

```
# My car loan
jenis mobil
kondisi baru
tahun 2025
nominal 300000000
tenor 4
dp 90000000
calculate
dp 105000000
calculate
```

3. **Run it:**

```sh
./credit_simulator my_loan.txt
```

```
> jenis mobil
Vehicle type set to Mobil
...
> calculate
Error: my_loan.txt:8: DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 105,000,000.00)
> dp 105000000
Down payment set to Rp. 105,000,000.00
> calculate
Monthly installments for Mobil Baru 2025, principal Rp. 195,000,000.00:
tahun 1 : Rp. 4,387,500.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 4,742,887.50/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 5,150,775.83/bln , Suku Bunga : 8,6%
tahun 4 : Rp. 5,598,893.32/bln , Suku Bunga : 8,7%
```

Errors include the **file name and line number** (`my_loan.txt:8`), and the run continues with the next
line.

4. **Check the exit code** (useful in scripts) with `echo $?` right after the run:

| Exit code | Meaning | Example |
|---|---|---|
| `0` | Every line succeeded | `./credit_simulator samples/file_inputs.txt` |
| `1` | The file ran, but at least one line failed | `my_loan.txt` above (line 8 failed) |
| `2` | The file couldn't be read, or too many arguments were given | `Cannot read file 'nope.txt': No such file` |

**Tips**

- Output goes to stdout and errors to stderr. To save only the results:
  `./credit_simulator my_loan.txt > results.txt`. To see only the errors:
  `./credit_simulator my_loan.txt 2>&1 >/dev/null`.
- `load` works in files too. In a file, `new` reads its answers from the next six lines:

```
new
Mobil
Bekas
2022
100000000
3
25000000
```

  If one of those answers is invalid, `new` stops and changes nothing, and the following lines run as
  normal commands.
- `./credit_simulator --help` prints a short usage message.

### UC7: Run with Docker instead of Java

**Goal:** use the app on a machine without Java.

1. Build the image once, from the project folder:

```sh
docker build -t credit-simulator .
```

2. Run it interactively (`-it` is required so you can type):

```sh
docker run -it --rm credit-simulator
```

3. Or run a file. Mount the folder that contains it and pass the path *inside* the container:

```sh
docker run --rm -v "$PWD/samples:/data:ro" credit-simulator /data/file_inputs.txt
```

4. To use another web service for `load`:

```sh
docker run -it --rm -e CREDIT_SIMULATOR_LOAD_URL=https://example.com/calculation.json credit-simulator
```

If the image has been published to Docker Hub, skip the build and use
`<dockerhub-user>/credit-simulator` instead of `credit-simulator`.

## 5. Input rules at a glance

| Field | Command | Accepted | Rule checked on `calculate` |
|---|---|---|---|
| Vehicle type | `jenis` | `Mobil` or `Motor`, any case | — |
| Condition | `kondisi` | `Baru` (new) or `Bekas` (used), any case | — |
| Year | `tahun` | Exactly 4 digits, e.g. `2022` | `Baru`: not older than *this year − 1*. Any vehicle: not later than *this year + 1* |
| Loan amount | `nominal` | Digits only, e.g. `100000000` | Greater than 0 and at most `1000000000` |
| Tenor | `tenor` | Whole years `1` to `6` | — |
| Down payment | `dp` | Digits only | `Baru`: ≥ 35% of loan; `Bekas`: ≥ 25% of loan; always less than the loan |

Write amounts **without** dots, commas or `Rp`: `100000000`, not `100.000.000`, `100,000,000` or
`Rp100jt`.

## 6. How to read the result

```
Monthly installments for Mobil Bekas 2022, principal Rp. 75,000,000.00:
tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
```

- **principal** is the amount you borrow: *loan amount − down payment*.
- **tahun N** is year N of the tenor. **Rp. …/bln** is the monthly installment during that year.
- **Suku Bunga** is that year's interest rate. It starts at 8% for `Mobil` and 9% for `Motor`, then goes
  up 0.1% into years 2, 4 and 6 and 0.5% into years 3 and 5.

| Year | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| Mobil | 8% | 8,1% | 8,6% | 8,7% | 9,2% | 9,3% |
| Motor | 9% | 9,1% | 9,6% | 9,7% | 10,2% | 10,3% |

**How each year is calculated** (the same method as `Rumus.xlsx`): that year's interest is added to the
remaining balance, and the total is divided over all remaining months of the tenor.

```
Example: Mobil, principal Rp 75,000,000, 3 years
Year 1: 75,000,000 × 1.080 = 81,000,000 ÷ 36 months = 2,250,000.00 / month
        remaining: 81,000,000 − 12 × 2,250,000 = 54,000,000
Year 2: 54,000,000 × 1.081 = 58,374,000 ÷ 24 months = 2,432,250.00 / month
        remaining: 58,374,000 − 12 × 2,432,250 = 29,187,000
Year 3: 29,187,000 × 1.086 = 31,697,082 ÷ 12 months = 2,641,423.50 / month
        remaining: 0
```

Amounts are calculated precisely and only rounded to 2 decimals when displayed.

## 7. Troubleshooting

| Problem | What to do |
|---|---|
| `credit_simulator: Java 17 or newer is required but 'java' was not found` (or `does not run`) | Install a JDK ([step 1](#step-1-install-java-17)), open a new terminal, retry. Or use [Docker](#uc7-run-with-docker-instead-of-java) |
| `Java 17 or newer is required, found Java 11` | Install JDK 17+ and either put it first on `PATH` or set `JAVA_HOME`, e.g. `export JAVA_HOME=$(/usr/libexec/java_home -v 17)` on macOS |
| `permission denied: ./credit_simulator` | `chmod +x credit_simulator bin/credit_simulator mvnw scripts/smoke_test.sh` |
| `credit_simulator: build failed` on the first run | The first build needs internet to download Maven and dependencies. Check the connection or proxy, then run `./mvnw package` to see the full error |
| `Error: Could not reach calculation service: ...` on `load` | The web service is down, expired or blocked (e.g. a certificate error behind a corporate proxy). Use another URL or the offline stub ([UC4](#use-a-different-web-service-or-work-offline)) |
| `Calculation service returned HTTP 404` | The URL is wrong or the mocky link expired; set `CREDIT_SIMULATOR_LOAD_URL` |
| `... must be a whole number of rupiah written with digits only` | Remove dots, commas and `Rp` from the amount |
| `Missing: ...` on `calculate` | Set the listed fields; the names are the commands to use (`tenor`, `dp`, …) |
| My edits changed a sheet I had saved | `save_sheet` switches to the new sheet. Save under a new name **before** editing ([UC5](#uc5-compare-loan-options-with-sheets)) |
| `Unexpected error: ...` | A bug. Rerun with `CREDIT_SIMULATOR_DEBUG=1 ./credit_simulator ...` to see the stack trace and report it |

## 8. Command reference

| Command | Example | What it does |
|---|---|---|
| `show` | `show` | List all commands |
| `new` | `new` | Ask for every field in order, then calculate |
| `jenis` | `jenis mobil` | Set vehicle type: `Mobil` or `Motor` |
| `kondisi` | `kondisi bekas` | Set condition: `Baru` or `Bekas` |
| `tahun` | `tahun 2022` | Set vehicle year (4 digits) |
| `nominal` | `nominal 100000000` | Set total loan amount (max 1000000000) |
| `tenor` | `tenor 3` | Set tenor in years (1–6) |
| `dp` | `dp 25000000` | Set down payment |
| `status` | `status` | Show the active sheet's inputs and last result |
| `calculate` | `calculate` | Validate and show the monthly installments |
| `load` | `load` | Fetch an existing calculation from the web service and calculate it |
| `save_sheet` | `save_sheet option-a` | Copy the active sheet to a new name and switch to it |
| `switch_sheet` | `switch_sheet default` | Switch to a sheet and show it |
| `list_sheets` | `list_sheets` | List all sheets (`*` = active) |
| `exit` | `exit` | Quit (or press `Ctrl+D`) |

| Environment variable | Purpose |
|---|---|
| `CREDIT_SIMULATOR_LOAD_URL` | Web service URL used by `load` |
| `CREDIT_SIMULATOR_DEBUG=1` | Show stack traces for unexpected errors |
