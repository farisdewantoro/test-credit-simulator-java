# credit_simulator

A console application that calculates the monthly installments of a vehicle loan (car or motorcycle,
new or used) for each year of a 1–6 year tenor. Loan details can be typed in, read from a text file, or
loaded from a JSON web service. Several calculations can be kept side by side as named **sheets**.

- **Language:** Java 17, built with Maven (via the bundled Maven Wrapper)
- **Runtime dependency:** Jackson, for the JSON web service only. Everything else is the JDK.
- **Design notes:** [`docs/TECH_DESIGN.md`](docs/TECH_DESIGN.md)

```
$ ./credit_simulator samples/file_inputs.txt
...
> calculate
Monthly installments for Mobil Bekas 2022, principal Rp. 75,000,000.00:
tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%
```

## Contents

1. [Requirements](#1-requirements)
2. [Run it](#2-run-it)
3. [Commands](#3-commands)
4. [Step-by-step walkthrough](#4-step-by-step-walkthrough)
5. [Business rules and calculation](#5-business-rules-and-calculation)
6. [Tests](#6-tests)
7. [Docker](#7-docker)
8. [CI/CD](#8-cicd)
9. [Configuration](#9-configuration)
10. [Project layout](#10-project-layout)

## 1. Requirements

- **JDK 17 or newer** on `PATH` (or `JAVA_HOME` set), on Linux or macOS.
- Internet access on the first run, so the Maven Wrapper can download Maven and the dependencies.

No other installation is needed: you don't need Maven installed. If you have no JDK, use the
[Docker image](#7-docker) instead.

## 2. Run it

```sh
# Interactive mode
./credit_simulator
bin/credit_simulator            # same thing

# File mode: run the commands in a text file, one per line
./credit_simulator samples/file_inputs.txt
bin/credit_simulator samples/file_inputs.txt
```

The first run builds `target/credit-simulator.jar`, which takes about a minute. Later runs start
immediately. The launcher rebuilds automatically when `pom.xml` or anything under `src/main` changes. To
build manually: `./mvnw package`.

**Input files.** One command per line, exactly as you would type it. Blank lines and lines starting with
`#` are ignored. Errors are reported with the file and line, e.g.
`Error: inputs.txt:7: Tenor must be between 1 and 6 years`. A failing line does not stop the run.

**Exit codes**

| Code | Meaning |
|---|---|
| `0` | Success (interactive mode always exits with 0) |
| `1` | File mode: the file was processed but at least one line failed |
| `2` | Usage error: too many arguments or an unreadable input file |

## 3. Commands

Type `show` at any time to list these. Commands are case-insensitive.

| Command | Description |
|---|---|
| `show` | List all available commands |
| `new` | Guided input: asks for every field in order, then calculates |
| `jenis <Mobil\|Motor>` | Set the vehicle type |
| `kondisi <Baru\|Bekas>` | Set the vehicle condition (new or used) |
| `tahun <yyyy>` | Set the vehicle year (4 digits) |
| `nominal <amount>` | Set the total loan amount in rupiah (max 1000000000) |
| `tenor <1-6>` | Set the loan tenor in years |
| `dp <amount>` | Set the down payment in rupiah |
| `status` | Show the active sheet's inputs and its last result |
| `calculate` | Validate the inputs and show the monthly installment for each year |
| `load` | Load an existing calculation from the web service and calculate it |
| `save_sheet <name>` | Save the active sheet's inputs and result as `<name>` and make it the active sheet |
| `switch_sheet <name>` | Make `<name>` the active sheet and show its inputs and result |
| `list_sheets` | List all sheets; `*` marks the active one |
| `exit` | Quit the application |

Amounts are plain digits (`100000000`). `.` and `,` separators are rejected because they mean different
things in Indonesian and US formats.

### Sheets

The application starts with one sheet called `default`. All editing commands work on the **active**
sheet.

- `save_sheet <name>` works like *Save as*. It copies the active sheet (inputs and result) to `<name>`
  and makes `<name>` active, so **later edits go to `<name>`**. The original sheet is unchanged. Saving
  over an existing name replaces that sheet.
- `switch_sheet <name>` makes another sheet active and shows its inputs and last result.
- Changing any input clears that sheet's result, so a sheet never shows a result that doesn't match its
  inputs.
- Sheet names are 1–32 letters, digits, `-` or `_`, matched case-insensitively. Sheets last for the
  current session only.

## 4. Step-by-step walkthrough

**a) Guided input (`new`).** Start `./credit_simulator`, type `new` and answer each question:

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

An invalid answer is explained and asked again.

**b) Field by field.** Set the fields in any order with `jenis`, `kondisi`, `tahun`, `nominal`, `tenor`
and `dp`, check them with `status`, then run `calculate`. If something is missing or breaks a rule,
every problem is listed at once:

```
> calculate
Error: Cannot calculate, 2 problems found:
  - Missing: tenor
  - DP must be at least 35% of loan amount for a Baru vehicle (min Rp. 35,000,000.00)
```

**c) Load an existing calculation.** `load` fetches the calculation from the web service, puts it in
the active sheet, and calculates it immediately:

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

If the loaded data breaks a rule (for example a `Baru` vehicle that has become too old), it stays in the
sheet so you can fix it, and the problems are shown. If the service is unreachable, the sheet is left as
it was.

**d) Sheets.** Save the result, start another calculation and switch between them:

```
> save_sheet mobil-bekas
Saved sheet 'mobil-bekas' from 'default'; 'mobil-bekas' is now the active sheet
> switch_sheet default
...
> jenis motor
... (enter the motorcycle loan and calculate)
> save_sheet motor-bekas
> list_sheets
  default      Motor/Bekas/2019  Rp. 20,000,000.00   1 thn  ✔ calculated
  mobil-bekas  Mobil/Bekas/2022  Rp. 100,000,000.00  3 thn  ✔ calculated
* motor-bekas  Motor/Bekas/2019  Rp. 20,000,000.00   1 thn  ✔ calculated
> switch_sheet mobil-bekas
```

[`samples/file_inputs.txt`](samples/file_inputs.txt) contains this whole session. Run it with
`./credit_simulator samples/file_inputs.txt`.

## 5. Business rules and calculation

| Rule | Value |
|---|---|
| Vehicle type / condition | `Mobil` or `Motor` / `Baru` (new) or `Bekas` (used), case-insensitive |
| Vehicle year | 4 digits; a `Baru` vehicle cannot be older than *current year − 1*; no later than *current year + 1* |
| Loan amount | Greater than 0 and at most Rp 1,000,000,000 |
| Tenor | 1 to 6 years |
| Down payment | At least 35% of the loan for `Baru`, 25% for `Bekas`; less than the loan amount |
| Base interest rate | Mobil 8%, Motor 9% |
| Rate increase | +0.1% going into years 2, 4, 6 and +0.5% going into years 3, 5 (8% → 8.1% → 8.6% → 8.7% → 9.2% → 9.3% for Mobil) |

**Calculation** (the same as `Rumus.xlsx`). The amount financed is *loan − DP*. Each year, that year's
interest is added to the remaining balance, and the total is spread over all remaining months of the
tenor:

```
total(year)   = balance × (1 + rate(year))
monthly(year) = total(year) / (12 × years remaining)
balance       = total(year) − 12 × monthly(year)
```

All arithmetic uses `BigDecimal` at full precision. Only the displayed amounts are rounded, half-up, to
2 decimals.

## 6. Tests

```sh
./mvnw test                                   # all unit and end-to-end tests
./mvnw test -Dtest=InstallmentCalculatorTest  # a single test class
./mvnw verify                                 # what CI runs
scripts/smoke_test.sh                         # runs the real launchers like a reviewer would
```

The tests are JUnit 5 only, with no mocking library. Reports go to `target/surefire-reports/`.

| Test | What it checks |
|---|---|
| `InstallmentCalculatorTest` | `Rumus.xlsx`, the web-service payload and a 1-year loan, to the cent; the loan is fully paid off |
| `SteppedInterestRatePolicyTest` | The interest schedule for both vehicle types, years 1–6 |
| `LoanValidatorTest` | Every rule at its boundary, e.g. DP exactly 35% passes but 1 rupiah less fails; all violations reported together |
| `InputParserTest`, `VehicleTypeTest`, `VehicleConditionTest` | Accepted and rejected input formats |
| `ConsoleFormatterTest` | `Rp. 3,500,000.00` and `8,1%` formats |
| `CommandFactoryTest`, `SimulatorControllerTest` | Command parsing, error handling, `show` lists every command |
| `NewCalculationCommandTest`, `SheetCommandsTest`, `WorkspaceTest` | Guided flow; save, switch and list sheets |
| `HttpExistingCalculationClientTest`, `LoadCommandTest` | `load` against a real in-process HTTP server: success, HTTP 500, bad JSON, missing field, unknown field, timeout |
| `CreditSimulatorApplicationE2ETest` | Runs the app on `samples/file_inputs.txt` and compares the output with `src/test/resources/expected_output.txt`; exit codes |

To try your own scenario, write the commands to a file and run `./credit_simulator yourfile.txt`. The
exit code is `1` if any line failed, so it can be used in scripts.

## 7. Docker

```sh
docker build -t credit-simulator .

docker run -it --rm credit-simulator                                  # interactive
docker run --rm -v "$PWD/samples:/data:ro" credit-simulator /data/file_inputs.txt   # file mode
```

The published image is `<dockerhub-user>/credit-simulator` (tags `latest`, `sha-<commit>` and the version
for `v*` tags), built for `linux/amd64` and `linux/arm64`.

## 8. CI/CD

GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)):

1. **test**, on every push and pull request, on Ubuntu and macOS: `./mvnw -B verify`, then
   `scripts/smoke_test.sh`. The test reports and the jar are uploaded as artifacts.
2. **docker**, after `test` passes, on pushes to `master` and `v*` tags: builds the image for amd64 and
   arm64, pushes it to Docker Hub, then runs the pushed image against the sample file.

To enable publishing, add these repository secrets: `DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN` (a Docker
Hub access token, not your password). The image is pushed as `$DOCKERHUB_USERNAME/credit-simulator`.

See [`docs/CI_CD_GUIDE.md`](docs/CI_CD_GUIDE.md) for step-by-step setup, releasing, running the pipeline
locally, troubleshooting and packaging the submission.

## 9. Configuration

| Environment variable | Default | Purpose |
|---|---|---|
| `CREDIT_SIMULATOR_LOAD_URL` | `https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c` | Web service used by `load` (mocky URLs can expire) |
| `CREDIT_SIMULATOR_DEBUG` | unset | Set to `1` to print stack traces for unexpected errors |

The web service must answer `GET` with:

```json
{"vehicleType": "Mobil", "vehicleCondition": "Baru", "vehicleYear": 2025,
 "totalLoanAmount": 1000000000, "loanTenure": 6, "downPayment": 500000000}
```

## 10. Project layout

```
credit_simulator              launcher (builds on first run, then runs the jar)
bin/credit_simulator          same launcher, alternative path
samples/file_inputs.txt       sample input file
scripts/smoke_test.sh         launcher smoke test (used by CI)
src/main/java/com/creditsimulator/
  CreditSimulatorApplication  main(): arguments, wiring, exit codes
  model/                      VehicleType, LoanDraft, LoanApplication, Sheet, Workspace, ...
  policy/                     interest-rate and down-payment rules + VehiclePolicyFactory
  service/                    LoanValidator, InstallmentCalculator, web-service client
  controller/                 SimulatorController and one Command class per command
  view/                       ConsoleView, PlainConsoleView, ConsoleFormatter
  io/                         console and file input
  exception/                  typed errors shown to the user
```

The structure is MVC with the Command, Factory and Strategy patterns. The calculator and validator have
no I/O, so they are tested directly. See [`docs/TECH_DESIGN.md`](docs/TECH_DESIGN.md) for the reasoning.
