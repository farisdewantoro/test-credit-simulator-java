# CI/CD Guide

How to set up, run, verify and troubleshoot the build → test → Docker Hub pipeline in
[`.github/workflows/ci.yml`](../.github/workflows/ci.yml).

> **Keep the repository private.** `docs/TECH_DESIGN.md` restates the confidential problem statement.
> Push only to a **private** GitHub repository. In a fresh clone, `origin` points to the public sample
> repository (`noveriojoee/credit_simulator`). Never push there. See step 1.

## Contents

1. [What the pipeline does](#what-the-pipeline-does)
2. [One-time setup](#one-time-setup)
3. [Run the pipeline](#run-the-pipeline)
4. [Release a version](#release-a-version)
5. [Check the published image](#check-the-published-image)
6. [Run CI locally before pushing](#run-ci-locally-before-pushing)
7. [Troubleshooting](#troubleshooting)
8. [Package the submission](#package-the-submission)

## What the pipeline does

```
push / pull request
        │
        ▼
┌──────────────────────────── test (ubuntu-latest, macos-latest) ────────────────────────────┐
│ checkout → setup-java 17 (Maven cache) → ./mvnw -B verify → scripts/smoke_test.sh          │
│ → upload surefire reports (+ jar on Ubuntu)                                                │
└────────────────────────────────────────────────────────────────────────────────────────────┘
        │ passed, and the event is a push to master or a v* tag
        ▼
┌──────────────────────────────────────── docker ────────────────────────────────────────────┐
│ QEMU + Buildx → log in to Docker Hub → compute tags → build linux/amd64 + linux/arm64       │
│ → push → run the pushed image on samples/file_inputs.txt and check the output              │
└────────────────────────────────────────────────────────────────────────────────────────────┘
```

| Event | `test` | `docker` |
|---|---|---|
| Pull request | ✔ | — |
| Push to any branch other than `master` | — (the workflow only triggers on `master`) | — |
| Push to `master` | ✔ | ✔ pushes `latest` and `sha-<commit>` |
| Push a tag `v1.2.3` | ✔ | ✔ pushes `1.2.3`, `1.2` and `sha-<commit>` |
| Manual run (*Run workflow* button) | ✔ | — |

A run where both jobs are green means:
- all unit and end-to-end tests pass on Linux and macOS;
- the real `./credit_simulator` and `bin/credit_simulator` launchers work on both systems;
- the published image runs and produces the expected installments.

## One-time setup

### 1. Create a private GitHub repository and point the project at it

With the [GitHub CLI](https://cli.github.com/) (`gh auth login` first):

```sh
cd credit_simulator

# Stop tracking the public sample repository
git remote remove origin

# Create a PRIVATE repo under your account, add it as origin and push master
gh repo create <your-github-user>/credit_simulator --private --source . --remote origin --push
```

Without the CLI: create an empty **private** repository on github.com (no README or licence), then:

```sh
git remote remove origin
git remote add origin git@github.com:<your-github-user>/credit_simulator.git
git push -u origin master
```

Check the remote before every push: `git remote -v`.

### 2. Create a Docker Hub access token

1. Sign in at <https://hub.docker.com>.
2. Go to **Account settings → Personal access tokens → Generate new token**.
3. Name it, for example `github-actions-credit-simulator`, and choose **Read & Write** access.
4. Copy the token now; Docker Hub shows it only once.

Use the token, never your password. The first push creates the `credit-simulator` repository under your
Docker Hub account. New repositories are public by default. If you want the image private, create
`<dockerhub-user>/credit-simulator` as private on Docker Hub before the first run.

### 3. Add the two repository secrets

| Secret | Value |
|---|---|
| `DOCKERHUB_USERNAME` | Your Docker Hub username (the image becomes `<username>/credit-simulator`) |
| `DOCKERHUB_TOKEN` | The access token from step 2 |

```sh
gh secret set DOCKERHUB_USERNAME --body "<dockerhub-user>"
gh secret set DOCKERHUB_TOKEN          # paste the token when prompted
gh secret list                         # both names should be listed
```

Or in the browser: **repository → Settings → Secrets and variables → Actions → New repository secret**.

## Run the pipeline

Any push to `master` runs both jobs. To start a run and follow it:

```sh
git push origin master
gh run watch                 # live view of the latest run
gh run list --limit 5        # recent runs and their status
gh run view --log-failed     # logs of the failed steps only
```

To test changes without publishing an image, open a pull request. Only the `test` job runs. You can
also use **Actions → CI → Run workflow**, which also runs only `test`.

**Artifacts.** Each run keeps `surefire-reports-ubuntu-latest`, `surefire-reports-macos-latest` and
`credit-simulator-jar`. Download them from the run page or with `gh run download <run-id>`.

## Release a version

Tag a commit on `master` with a `v`-prefixed semantic version and push the tag:

```sh
git tag -a v1.0.0 -m "credit_simulator 1.0.0"
git push origin v1.0.0
```

The `docker` job then pushes `<dockerhub-user>/credit-simulator:1.0.0`, `:1.0` and `:sha-<commit>`.
`latest` only moves on pushes to `master`.

## Check the published image

```sh
docker pull <dockerhub-user>/credit-simulator:latest

# File mode with the sample file
docker run --rm -v "$PWD/samples:/data:ro" <dockerhub-user>/credit-simulator /data/file_inputs.txt

# Interactive mode
docker run -it --rm <dockerhub-user>/credit-simulator

# Both architectures are published
docker buildx imagetools inspect <dockerhub-user>/credit-simulator:latest | grep Platform
```

Expected output includes:

```
tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%
tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%
```

`imagetools inspect` lists `linux/amd64` and `linux/arm64`. It also lists two `unknown/unknown` entries,
which are Buildx's provenance attestations, not images.

## Run CI locally before pushing

You can reproduce each part of the pipeline locally. These commands are how the workflow was verified.

**Same steps as the `test` job** (needs only a JDK 17):

```sh
./mvnw -B verify
scripts/smoke_test.sh
```

**Lint the workflow file** with [actionlint](https://github.com/rhysd/actionlint)
(`brew install actionlint`):

```sh
actionlint .github/workflows/ci.yml        # no output means no problems
```

**Run the `test` job in a GitHub-like Linux container** with [act](https://github.com/nektos/act)
(`brew install act`, Docker running). act runs Linux jobs only, so it covers the `ubuntu-latest` half of
the matrix:

```sh
act push -j test --matrix os:ubuntu-latest \
  -P ubuntu-latest=catthehacker/ubuntu:act-latest \
  --artifact-server-path /tmp/act-artifacts
# ends with: 🏁  Job succeeded
```

Run act from a clean clone (`git clone . /tmp/cs && cd /tmp/cs`) so the container doesn't write
root-owned files into your `target/`.

**Rehearse the `docker` job without Docker Hub,** using a throwaway local registry:

```sh
docker run -d --rm --name cs-registry -p 5055:5000 registry:2
docker buildx create --name cs-ci --driver docker-container --driver-opt network=host
docker buildx build --builder cs-ci --platform linux/amd64,linux/arm64 \
  -t localhost:5055/credit-simulator:test --push .

for p in linux/amd64 linux/arm64; do
  docker run --rm --platform "$p" -v "$PWD/samples:/data:ro" \
    localhost:5055/credit-simulator:test /data/file_inputs.txt \
    | grep -F "tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%"
done

# clean up
docker stop cs-registry && docker buildx rm cs-ci
```

The only part this skips is the Docker Hub login and push, which needs your secrets.

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `docker` job, *Log in to Docker Hub*: `Username and password required` | `DOCKERHUB_USERNAME` or `DOCKERHUB_TOKEN` secret is missing (or was added as an environment secret or a variable instead of a repository secret) | Add both as **repository secrets** (step 3) and re-run the job: `gh run rerun <run-id> --failed` |
| `docker` job: `unauthorized: incorrect username or password` | Wrong token, expired token, or a password used instead of a token | Generate a new **Read & Write** token and update `DOCKERHUB_TOKEN` |
| `docker` job: `denied: requested access to the resource is denied` | Token is read-only, or the username doesn't own the namespace | Use a Read & Write token for the same account as `DOCKERHUB_USERNAME` |
| `docker` job never starts | The run was a pull request, a manual run, or a push to a branch other than `master` | Expected; push to `master` or push a `v*` tag |
| `docker` job is slow (several minutes) | The `linux/arm64` image is built under QEMU emulation | Expected. Later runs are faster thanks to the GitHub Actions build cache (`cache-from: type=gha`) |
| `test` job fails at `./mvnw` with `Permission denied` | The executable bit was lost (e.g. the project was copied through Windows or a zip) | `git update-index --chmod=+x mvnw credit_simulator bin/credit_simulator scripts/smoke_test.sh`, commit, push |
| `test` job fails at *Launcher smoke test* | A launcher or the sample output changed | Run `scripts/smoke_test.sh` locally; it prints which check failed |
| `CreditSimulatorApplicationE2ETest` fails after an intended output change | The golden file is out of date | Regenerate: `./mvnw -q -DskipTests package && java -jar target/credit-simulator.jar samples/file_inputs.txt > src/test/resources/expected_output.txt`, review the diff, commit |
| `load` fails in the app with a certificate or HTTP error | The mocky.io endpoint expired or a network proxy intercepts TLS | Not a CI problem: CI tests use a local stub server. Point `CREDIT_SIMULATOR_LOAD_URL` at a working endpoint |

## Package the submission

The submission is a tarball that **includes `.git`**. `git archive` leaves `.git` out, so use `tar`, and
exclude build output and the confidential PDF:

```sh
cd credit_simulator
./mvnw -q clean                                  # remove target/
git status --short                               # should be empty: everything committed
cd ..
tar --exclude='credit_simulator/target' \
    --exclude='credit_simulator/TechTest Backend.pdf' \
    --exclude='credit_simulator/.idea' --exclude='credit_simulator/.vscode' \
    -czf credit_simulator.tar.gz credit_simulator

# Check the contents: .git is included; no PDF, jar or class files
tar -tzf credit_simulator.tar.gz | grep -E '/\.git/HEAD$'
tar -tzf credit_simulator.tar.gz | grep -Ei '\.(pdf|jar|class)$' || echo "clean"
```

Then share the private repository link (add the reviewers as collaborators) together with the tarball.
