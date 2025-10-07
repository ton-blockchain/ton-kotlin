# Continuous Integration & Release

This project uses two GitHub Actions workflows.

## CI (`.github/workflows/validation.yml`)
- Runs on every push to `main`, on pull requests, and manually via *Run workflow*.
- Validates the Gradle wrapper before any build steps run.
- `linux` job executes `./gradlew clean jvmTest linuxX64Test` with Temurin JDK 17 and caches both Gradle and Kotlin/Native artifacts.
- `windows` job runs `mingwX64Test` under the same caching strategy.
- `macos` job drives `macosArm64Test` on a macOS 14 runner.
- Static analysis (`detekt`, `ktlintCheck`, `checkLegacyAbi`) and Dokka generation run opportunistically when the tasks exist.

### Local parity
```
GRADLE_USER_HOME=.gradle ./gradlew clean jvmTest linuxX64Test
GRADLE_USER_HOME=.gradle ./gradlew mingwX64Test
GRADLE_USER_HOME=.gradle ./gradlew macosArm64Test
GRADLE_USER_HOME=.gradle ./gradlew detekt
GRADLE_USER_HOME=.gradle ./gradlew checkLegacyAbi
```

## Release (`.github/workflows/release.yml`)
- Triggered on annotated tags matching `v*` or manually.
- Runs the same Linux (`clean check`) and macOS (`macosArm64Test`) gates before any publishing.
- Publishes artifacts to both GitHub Packages and Maven Central, then closes/releases the Sonatype staging repository.
- Generates release notes via `git-cliff` using `git-cliff.toml` and posts them to the GitHub Release.

### Required secrets
| Secret | Purpose |
| --- | --- |
| `MAVEN_CENTRAL_USERNAME` | Sonatype OSSRH username. |
| `MAVEN_CENTRAL_PASSWORD` | Sonatype OSSRH password. |
| `SIGNING_KEY` | Base64-encoded ASCII-armored PGP key for publication signing. |
| `SIGNING_PASSWORD` | Password for the signing key. |
| `GITHUB_TOKEN` (provided) | Used for publishing to GitHub Packages and creating releases. |

Expose the credentials to Gradle by adding repository secrets with `ORG_GRADLE_PROJECT_` prefixes as done in the workflow.

### Manual release checklist
1. Update the project version in `build.gradle.kts` if needed and tag `vX.Y.Z`.
2. Push the tag or trigger the workflow manually from the Actions tab.
3. Confirm both package repositories contain the new release and that the GitHub Release contains the generated changelog snippet.

### Disabled tests
- `BitStringTest.BitString concatenation with double-shifting`
- `CellBuilderTest.build a number`
- VM stack round-trip tests in `block-tlb`
- `WalletV4Example.walletExample`

These suites are marked with `@Ignore` because they either require network access or currently fail on Kotlin/Native Apple targets. Re-enable once fixed.

### Static analysis baseline
Detekt uses per-module baselines in `detekt-baselines/`. Regenerate them after intentional clean-ups via `./gradlew detektBaseline` so CI stays green.

### Binary compatibility baseline
Kotlin's ABI validation stores reference dumps under each module's `api/` directory. When intentionally updating public APIs, run `./gradlew updateLegacyAbi` (after verifying compatibility) and commit the regenerated `.api`/`.klib.api` files.
