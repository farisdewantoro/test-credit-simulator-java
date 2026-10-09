#!/bin/sh
# ----------------------------------------------------------------------------
# Smoke test for the launchers: runs the real ./credit_simulator and
# bin/credit_simulator the way a reviewer would and checks output and exit codes.
#
#   scripts/smoke_test.sh
# ----------------------------------------------------------------------------
set -u

ROOT=$(cd "$(dirname "$0")/.." && pwd -P)
SAMPLE="$ROOT/samples/file_inputs.txt"
WORK=$(mktemp -d "${TMPDIR:-/tmp}/credit_simulator_smoke.XXXXXX")
trap 'rm -rf "$WORK"' EXIT INT TERM

failures=0
pass() { echo "  ok   $1"; }
fail() { echo "  FAIL $1" >&2; failures=$((failures + 1)); }

# expect_exit <name> <expected code> <actual code>
expect_exit() {
  if [ "$3" -eq "$2" ]; then pass "$1: exit code $2"; else fail "$1: expected exit code $2, got $3"; fi
}

# expect_output <name> <file> <fixed string>
expect_output() {
  if grep -qF -- "$3" "$2"; then pass "$1: prints '$3'"; else fail "$1: missing '$3'"; fi
}

check_sample_run() {
  name=$1
  shift
  "$@" "$SAMPLE" >"$WORK/out" 2>"$WORK/err"
  expect_exit "$name" 0 $?
  expect_output "$name" "$WORK/out" "tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%"
  expect_output "$name" "$WORK/out" "tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%"
  expect_output "$name" "$WORK/out" "tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"
  expect_output "$name" "$WORK/out" "tahun 1 : Rp. 1,362,500.00/bln , Suku Bunga : 9%"
  expect_output "$name" "$WORK/out" "* motor-bekas"
  expect_output "$name" "$WORK/out" "Switched to sheet 'mobil-bekas'"
  expect_output "$name" "$WORK/out" "Available commands:"
  # The launcher's one-off "building" notice is expected on a fresh checkout.
  if grep -v '^credit_simulator: building' "$WORK/err" >"$WORK/err.real"; then
    fail "$name: unexpected stderr: $(cat "$WORK/err.real")"
  else
    pass "$name: no errors"
  fi
}

echo "credit_simulator smoke test"

check_sample_run "./credit_simulator file" "$ROOT/credit_simulator"
check_sample_run "bin/credit_simulator file" "$ROOT/bin/credit_simulator"

# Works from any working directory, with a relative input path.
(cd "$ROOT/samples" && "$ROOT/credit_simulator" file_inputs.txt) >"$WORK/out" 2>&1
expect_exit "run from another directory" 0 $?

# Interactive mode reads commands from stdin.
printf 'show\njenis mobil\nstatus\nexit\n' | "$ROOT/credit_simulator" >"$WORK/out" 2>&1
expect_exit "interactive" 0 $?
expect_output "interactive" "$WORK/out" "Available commands:"
expect_output "interactive" "$WORK/out" "jenis   : Mobil"

# A failing line is reported with file:line and gives exit code 1.
printf 'jenis mobil\ntenor 9\n' >"$WORK/bad.txt"
"$ROOT/credit_simulator" "$WORK/bad.txt" >"$WORK/out" 2>"$WORK/err"
expect_exit "file with an invalid line" 1 $?
expect_output "file with an invalid line" "$WORK/err" "bad.txt:2: Tenor must be between 1 and 6 years"

# A missing file is a usage error: exit code 2.
"$ROOT/credit_simulator" "$WORK/missing.txt" >"$WORK/out" 2>"$WORK/err"
expect_exit "missing file" 2 $?
expect_output "missing file" "$WORK/err" "Cannot read file"

if [ "$failures" -eq 0 ]; then
  echo "All smoke tests passed."
else
  echo "$failures smoke test check(s) failed." >&2
  exit 1
fi
