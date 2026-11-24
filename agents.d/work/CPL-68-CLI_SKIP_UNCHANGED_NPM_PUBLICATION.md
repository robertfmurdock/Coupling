# CPL-68 — Publish the CLI to npm only when its package changes

**Work card protocol: 2**

## Goal and recommendation

Skip publication of `@continuous-excellence/coupling-cli` when the candidate npm
package matches the version currently designated `latest` in npm. Compare the
deliverable, including compiled shared dependencies, rather than Git paths or
commit messages. Keep Coupling's existing application version calculation, Git
tags/releases, deployments, JVM distribution, and S3 uploads independent of this
decision. An npm version may legitimately lag the application version or have gaps.

Status: reopened after live CI exposed npm 12 response incompatibility. Prior
slices: `d5cd275d4`, `68e00ae80`; compatibility correction is locally validated,
awaiting owner push and live CI verification. The owner
pushed those commits; no agent push or release is authorized.
One contributor owns each dependent slice through tests, implementation, review,
full checks, and commit. No push or release is authorized by this card.

## Planning findings and source references

- The sibling repository's `../testmints-js/scripts/compare-release.mjs` compares
  the candidate tarball with the exact npm `latest` version. It compares paths,
  contents and executable bits, ignores archive metadata and directory entries,
  and compares manifest JSON without top-level `version` and `devDependencies`.
  README/LICENSE changes count. Lookup/comparison failures fail the release.
- Its `docs/publishing.md` and `.github/workflows/release.yml` put consumer
  verification before comparison and publish the compared tarball. An identical
  package gets neither publication nor a Git tag. Its bootstrap exception requires
  a sole `0.0.0` tag. Those tag/bootstrap rules belong to that single-package repo;
  do not transplant them into Coupling.
- Here, [cli/build.gradle.kts](../../cli/build.gradle.kts) builds `jsCliTar` as a
  Gradle tar of the generated npm project, but `jsPublish` runs `npm publish` on
  the directory. The tar is not proof of npm's actual selected package contents.
  `jsPublish` is disabled for snapshots and ordered after checks, root release,
  and production deployment when those tasks are present. `mustRunAfter` does
  not itself schedule or require those tasks.
- At planning time the build generated `Versions.couplingVersion` from the root
  version in [Versions.kt](../../cli/src/jvmMain/templates/Versions.kt) (then common,
  now JVM-only).
  [CouplingCli](../../cli/src/commonMain/kotlin/com/zegreatrob/coupling/cli/CouplingCli.kt)
  used it for `--version`. Ignoring only manifest version would still detect a
  compiled-code change on every release. Generated source maps and any local
  package dependency versions also need inspection; their actual packed form
  has not yet been established.
- [.github/workflows/main.yml](../../.github/workflows/main.yml) runs
  `check release` with Tagger's calculated version; root Tagger releases master
  and enables GitHub releases. Inspect the plugin-provided release task graph
  before modifying wiring; the direct CLI build script does not establish all
  release dependencies or failure propagation.
- [CPL-33](../work/CPL-33-05_CDN_LOOKUP_COUPLING_MIGRATION_AND_RELEASE.md) concerns a
  different npm package. It is context, not a prerequisite or a reason to create
  a shared multi-package publishing framework.

## Fit with repository philosophy

Follow [owner values](../context/PERSONA.md): preserve integration flow, make
small useful releases, and prefer clear boundaries over clever comparison rules.
The recurring need is suppressing redundant CLI npm releases during application
delivery; the owner is `:cli`. This warrants maintained packaging/comparison
behavior, not a general release platform or retained investigation harness.

Follow the [Gradle playbook](../context/GRADLE_PLAYBOOK.md) and
[Actions playbook](../context/GITHUB_ACTIONS_PLAYBOOK.md): use typed Kotlin tasks
in the existing build-logic boundary, configured locally by `:cli`, with lazy
artifact providers and the same entry points locally and in CI. Reuse npm for
packing and registry artifact retrieval. Keep authentication/setup in existing
workflow orchestration; do not copy Testmints' Node/shell comparison implementation
or put policy in YAML. Keep one-off probes outside tracked source and retain
findings here. Do not expand scope into OIDC migration or unrelated release repair.

## Package equality and version design

1. Produce one real `npm pack` tarball, install/verify that artifact, compare it,
   and publish that exact file. Keep current non-npm distributions unchanged.
2. Prefer making the JS CLI read its own installed manifest version through a
   small platform boundary, retaining generated version behavior for JVM.
   Preserve current `--version` output and development/test execution. This
   removes release-only churn at its source without masking compiled changes.
   First inspect Kotlin/JS module layout and existing version tests; do not
   assume a working-directory-relative manifest lookup or an ESM-incompatible
   `require` path. Avoid an environment variable overriding the installed version.
3. Compare package-relative file paths, bytes and executable bits. Ignore tar/gzip
   timestamps, ownership, entry order, directory entries and non-executable mode
   differences. Compare `package.json` structurally, ignoring only its top-level
   `version` initially. Unlike Testmints, do not exclude `devDependencies` without
   evidence that doing so is necessary and harmless for this package.
4. Runtime dependency ranges, bin/main/exports/engines metadata, documentation,
   added/deleted files and source maps count as changes. Do not broadly replace
   version strings or omit generated JS/maps. If two builds at different release
   versions still differ, identify the exact producer and smallest justified
   correction before enabling the gate. Record any additional normalization here.
5. Resolve `latest` once per publication attempt, fetch that exact version, and
   compare with it. Equal means successful npm no-op; different means eligible
   for existing publication. Missing package/latest, invalid metadata, fetch or
   archive errors must fail with a useful diagnostic. This existing package needs
   no automatic bootstrap exception. Do not convert network failure into equality
   or permission to publish. Validate archive paths/types/duplicates before using
   contents; avoid unsafe extraction.
6. Registry resolution/comparison and publication are external-state operations;
   never reuse yesterday's registry decision from Gradle up-to-date/build cache.
   Deterministic package generation/comparison logic can reuse declared inputs.
   Tie any decision output to the exact candidate and baseline; publication must
   not accept a stale report. Emit a concise baseline and changed-path/no-op reason.
7. Preserve snapshot exclusion and existing release ordering. Ensure comparison
   failure prevents npm invocation on every supported entry point, including
   direct `:cli:publish`/`:cli:jsPublish`. Do not imply that npm failure rolls back
   an application Git tag or deployment that already happened. Inspect existing
   serialization before changing it; do not introduce a new release coordinator.

## Safe slice sequence

### Slice 1 — Canonical npm artifact with accurate installed version

Independent value: the installed CLI identifies its actual package version and
publication uses an artifact already verified as a consumer, even before skipping
is enabled. Owner: one contributor across CLI source, packaging and build logic.

Before tests, state the behavior, platform boundary and expected failure under
[Testing](../context/TESTING.md); use the Testmints skill for Testmints edits.
Read adjacent tests and inspect the current npm pack output with temporary probes.
Cover isolated installation and actual executable `--version`/`--help` without
application credentials, invocation from another working directory, and existing
JVM/development version behavior. Establish the meaningful failure for the new
manifest-owned JS version contract before editing production code.

Then declare the smallest implementation and data flow: manifest-owned JS version,
unchanged JVM version provider, `:cli` npm-pack output consumed by verification and
`jsPublish`. Extend existing capabilities; avoid a parallel packaging framework.
Use two different release versions with otherwise identical inputs to prove that
only the explicitly ignored manifest field changes. Treat any additional differences
as findings to resolve, not permission to broaden exclusions. Keep `jsCliTar`/S3
contracts intact. If actual pack/install exposes an existing unrelated defect,
record it and bound the necessary fix explicitly.

Run focused CLI/package checks, separately review test intent and production
design, pass `./gradlew check`, and commit tests plus implementation together.
Record exact task names and evidence here before beginning slice 2.

### Slice 2 — Fail-closed comparison gates npm publication

Independent value: redundant npm releases stop while changed packages continue
through the established release path. Owner: the contributor carrying this slice.

Before tests, declare the contract at the build-logic/task boundary. Expected red:
an identical candidate still invokes publication today. Test identical contents
across release versions/archive metadata, changed compiled/shared code, runtime
dependencies or executable bits, file addition/deletion, and malformed archives.
Test registry errors and missing baseline as failures, snapshot exclusion, and
direct publication task entry points. Use existing build-logic tests/TestKit and
controlled registry/process collaborators; never publish real packages in tests.
Assert observable publish/no-publish/failure behavior, not build-script text.

After meaningful red, declare the smallest implementation: candidate artifact
provider → fresh exact baseline retrieval → comparison → guarded publication of
the same artifact. Keep comparison logic bounded and testable, and add only the
task composition coverage needed to prove all publish paths honor its result.
Expose a non-publishing Gradle comparison entry point for local inspection and
document its command and failure behavior. Prefer no workflow edit; if needed,
retain thin orchestration and validate the changed execution path.

Run focused build-logic/task checks, separately review tests and implementation,
pass `./gradlew check`, review the full slice, and commit. No live release is
required to commit this independently useful change.

## Acceptance evidence and completion

- [x] Installed packed CLI reports its manifest version from an unrelated working
  directory; JVM/development behavior remains covered by owning suites.
- [x] Two otherwise identical builds at distinct release versions compare equal;
  changed runtime contents/metadata compare different. Record the actual packed
  layout and all supported exclusions.
- [x] Controlled task-boundary evidence shows equality invokes no npm publication,
  changes publish the compared artifact, errors prevent publication, and snapshots
  retain their existing behavior. Cover both public CLI publication task paths.
- [x] Confirm application release, deployment, JVM and S3 wiring is unaffected by
  npm equality; use task-graph/composition evidence rather than source assertions.
- [x] Record focused commands, full `./gradlew check` results and slice commits.
  Follow the documented external-sandbox/fish Gradle execution guidance. Full
  checks alone cannot prove npm comparison, so retain the bounded evidence above.
- [x] Document the local non-publishing command and intentional application/npm
  version gaps. Any live CI/npm observation requires separate push/release
  authorization; record it if available, otherwise label it unverified without
  inventing a follow-up monitoring system.
- [ ] Review overall outcome, update relevant links, and archive when the required
  implementation evidence is complete under [work-card rules](../context/WORK_CHECKLIST.md).

## Delivery evidence

### Live CI compatibility correction

- [Build 45313023650](https://github.com/robertfmurdock/Coupling/actions/runs/45313023650)
  for `528acaf62` passed checks, packed-consumer verification, application tag/release
  `1.1.920`, and production deployment, then failed in `:cli:jsPublish` while
  resolving npm latest. No npm publication occurred; latest remains `1.1.919`.
- Existing Actions setup upgrades npm to latest: this run used npm 12.2.3. Its
  `npm view <package>@latest version --json` returns `["1.1.919"]`, whereas local
  npm 11.20.0 returns `"1.1.919"`. Both responses were reproduced directly; the
  scalar-only gate incorrectly rejected the valid singleton array.
- Correction contract: accept a version string or exactly one version string in
  an array, still rejecting empty/multiple/non-string results and malformed semver.
  Resolve once and fetch that exact version. No YAML, registry-failure policy,
  package-equality rules or publication ordering changes are needed.
- The actual npm 12 non-publishing probe then exposed its second shape change:
  `npm pack --json` returns an object keyed by the package name; npm 11 returns
  an array. The producer's existing single-child reader already worked in CI,
  but the baseline reader required an array. Its regression also failed before
  correction. Accept only a single-item array or a single-key object for the
  requested package, then retain filename and archive identity validation.
- Add a controlled task regression for the observed array and fail-closed array
  variations, then validate actual npm 11/12 through the non-publishing task and
  run the full suite before committing. The owner must push the correction before
  another live CI result can establish success.
- Focused correction checks pass: `./gradlew :coupling-plugins:test --tests
  '*Npm*Test' :coupling-plugins:lintKotlin --console=plain`. The observed npm 12
  lookup and pack shapes each produced a meaningful failure before correction.
  Empty/ambiguous/non-string lookup arrays and empty/wrong-package/multiple-artifact
  pack results continue to prevent publication through both public task names.
- Real registry comparison now passes with npm 12.2.3 through a temporary init
  setting the comparison task's npm command to an isolated `npm exec` installation.
  The ordinary npm 11.20.0 `:cli:compareNpmPackage` also passes. Both fetch exact
  baseline `1.1.919` and report the candidate changed; neither publishes.
- Test review confirms both real response shapes exercise retrieval at the task
  boundary; production review retains exact-one-result checks, requested package
  identity, filename safety and archive validation. No equality normalization or
  workflow change was introduced.
- Correction full gate: `./gradlew check --console=plain` passed in 3m 16s,
  including 35 build-logic tests, CLI platform suites, SDK integration, browser
  and E2E tests. `git diff --check` passed. Next action: push the correction and
  watch the resulting CI run; do not rerun the unchanged failing commit.

### Initial implementation

- Direct `./gradlew` works; no fish wrapper is needed in this environment.
- `./gradlew :cli:jsCliTar --no-daemon` and
  `./gradlew check release --dry-run --no-daemon` passed. The latter shows
  application tag/release and deployment before CLI npm publication, followed by
  GitHub release finalization. Preserve that existing ordering.
- Actual `npm pack` of the generated project contains 1,890 files (8,183,505
  unpacked bytes), including compiled shared modules and source maps. No bundled
  node_modules; runtime dependencies are ordinary manifest dependencies.
- Isolated installation under `/tmp`, invoked from `/tmp`, runs on shell
  Node 26.13.1 with the existing launcher (repository pin: 26.13.0). Manifest version is
  `0.0.0-unspecified` but executable reports `unspecified`: concrete evidence of
  the generated-version mismatch. The packed-consumer Gradle check will retain
  this contract. No real publication or release has run.
- Slice 1 red: `:cli:verifyNpmPackage` failed with expected manifest version
  `0.0.0-unspecified`, actual `coupling-cli version unspecified`. Initial consumer
  setup needed its own private manifest so npm would not walk to a parent project;
  that setup failure was corrected before counting the behavior failure.
- Slice 1 focused green: `./gradlew :cli:verifyNpmPackage :cli:jsNodeTest
  :cli:jvmTest --no-daemon --console=plain` passed, including 23 tests per platform.
  Consumer execution strips application credentials and supplies a deliberately
  wrong `COUPLING_VERSION` to prove it cannot override the installed manifest.
- Version-input finding: `KotlinPackageJsonTask` did not invalidate its working
  package manifest on project-version changes (public manifest changed correctly).
  Declaring `cliVersion` on the owning CLI package tasks corrects this producer;
  packing also rejects a stale manifest. This was not merely a configuration-cache
  issue: disabling that cache alone did not correct the working manifest.
- Two normal `:cli:verifyNpmPackage` builds at `-Pversion=0.0.0-cpl68.1` and
  `-Pversion=0.0.0-cpl68.2` passed. Temporary Python tar inspection compared all
  1,890 paths, bytes and executable bits: zero differences after removing only
  top-level manifest `version`. All compiled shared modules and maps count.
- Test review: existing common version scenario exercises the CLI command on
  both platforms; the packed-consumer check covers installed layout, executable,
  unrelated working directory, help and credential independence. Production
  review: only JVM retains generated version source; JS locates the nearest
  manifest from its module URL; lazy artifact providers connect pack/verify/publish.
  No changes to JVM/S3 distribution contracts or wiring, workflow or application versioning.
- Slice 1 full `./gradlew check --console=plain` passed in 2m 56s (699 actionable
  tasks), including SDK integration, real-browser and E2E checks. Complete slice
  review and `git diff --check` passed; no push or release performed.
- Slice 1 commit: `d5cd275d4` (`Verify and publish the canonical CLI npm artifact`).
- Slice 2 red: the controlled TestKit equality scenario observed an npm publish
  command against an identical candidate. A strict-metadata scenario also exposed
  acceptance of malformed semver before that validation was tightened.
- Slice 2 focused green: `./gradlew :coupling-plugins:test --tests '*Npm*Test'
  :coupling-plugins:lintKotlin --console=plain`. TestKit uses the production task
  registration with disabled artifact producers and a recording local npm process;
  it covers both publication names, equality, changed artifacts, missing/invalid
  latest, lookup/fetch/archive/baseline errors, snapshots, publication failure,
  non-publishing comparison, and a second registry resolution with configuration
  cache reused. Archive tests cover file/metadata changes and malformed input.
- Publication owns its fresh registry resolution and exact artifact comparison in
  one task action. No decision report exists to become stale; tasks declare no
  reusable external-state output and explicitly disable build caching. Baseline
  downloads disable lifecycle scripts, and archives are validated without extraction.
- Real release graph probe: `check release --dry-run -Pversion=1.2.3` retains
  check → tag → release → production deployment → JVM distribution/S3 upload →
  CLI publication → GitHub release. Pack/consumer verification precede check.
  A snapshot graph at `1.2.3-SNAPSHOT` confirms `:cli:jsPublish` is disabled.
  Temporary init probes only print selected graph nodes; no release actions ran.
  The controlled delivery composition additionally executes application/deployment,
  JVM and S3 stand-ins successfully when npm equality produces a no-op.
- Local inspection command and intentional application/npm version gaps are
  documented in [CLI packaging](../../cli/README.md). Live npm publication and CI
  release observation remain unverified and require separate authorization.
- Independent relinking exposed three nondeterministic dependency files:
  `BufferedChannel.mjs`, `ChannelCoroutine.mjs`, and `WebSocketChannelsConfig.mjs`.
  Changes were `SendChannel`/`ReceiveChannel` import and interface metadata order.
  The installed Kotlin 2.4.21 compiler source artifact identifies the producer:
  `UselessDeclarationsRemover.collectUsedSuperTypes()` expands removed supertypes
  using `toHashSet()`. No package comparison exclusions were added.
- The CLI production linker keeps `kotlinx.coroutines.channels.Channel` to avoid
  that expansion and disables member-name minification. Keeping it alone failed
  the packed-consumer check with a mismatched `iterator_jk1svi_k$` call; consistent
  unminified member names restore executable behavior. This is scoped to the CLI
  production linker and must be re-evaluated when Kotlin is upgraded. Packed size
  increased from 1,660,850 to 1,758,092 bytes (5.9%); files remain 1,890.
- Final independent-link evidence: `:cli:verifyNpmPackage` at
  `-Pversion=0.0.0-cpl68.3` passed; then
  `./gradlew :cli:compileProductionExecutableKotlinJs --rerun :cli:compareNpmPackage
  -Pversion=0.0.0-cpl68.4 --no-configuration-cache --no-daemon --console=plain`
  with a temporary init supplying the actual `.3` tarball as a controlled npm
  baseline passed. The linker executed again, consumer verification passed, and
  the production comparator reported `npm package unchanged; skipping publication`.
  The temporary npm stand-in rejects publication commands; no live baseline or
  publish request was used. This supplements the earlier incremental-build proof.
- Separate final reviews: tests observe process commands and task outcomes through
  production registration, with archive variations below that boundary. Production
  review confirmed fresh registry state, exact baseline identity, no extraction,
  strict metadata, and unchanged foreign release ownership. Compiler instability
  was corrected at its producer rather than normalized in comparison.
- npm lookup, retrieval and publication explicitly retain the previous generated
  package working directory, supplied by the artifact provider. Controlled tests
  assert this directory independently of the candidate tarball's directory, so
  registry configuration context remains consistent. The final focused run passes
  15 npm tests plus build-logic lint.
- Final unfiltered `./gradlew check --console=plain` passed in 3m 7s, with 699
  actionable tasks, including 34 build-logic tests, 23 tests per CLI platform,
  SDK integration, client browser and E2E checks. `git diff --check` passed.
- After that full suite, the actual default-version packed artifact also compared
  equal to the controlled `.3` baseline via `:cli:compareNpmPackage`; consumer
  verification and comparison passed with the final npm working-directory wiring.
- Slice 2 commit: `68e00ae80` (`Skip unchanged CLI npm publications`). Overall
  outcome reviewed: the exact verified tarball is compared and conditionally
  published, application delivery remains independent, and failures stop npm
  publication. All required local evidence is complete; live release observation
  remains intentionally unverified. No inbound card links required updating.
