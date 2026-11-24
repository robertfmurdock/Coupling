# CPL-68 — Publish the CLI to npm only when its package changes

**Work card protocol: 2**

## Goal and recommendation

Skip publication of `@continuous-excellence/coupling-cli` when the candidate npm
package matches the version currently designated `latest` in npm. Compare the
deliverable, including compiled shared dependencies, rather than Git paths or
commit messages. Keep Coupling's existing application version calculation, Git
tags/releases, deployments, JVM distribution, and S3 uploads independent of this
decision. An npm version may legitimately lag the application version or have gaps.

Status: slice 1 in progress; implementation and validation underway.
One contributor owns each dependent slice through tests, implementation, review,
full checks, and commit. No push or release is authorized by this card.

## Findings and source references

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
- The same build generates `Versions.couplingVersion` from the root version in
  [Versions.kt](../../cli/src/commonMain/templates/Versions.kt).
  [CouplingCli](../../cli/src/commonMain/kotlin/com/zegreatrob/coupling/cli/CouplingCli.kt)
  uses it for `--version`. Ignoring only manifest version would still detect a
  compiled-code change on every release. Generated source maps and any local
  package dependency versions also need inspection; their actual packed form
  has not yet been established.
- [.github/workflows/main.yml](../../.github/workflows/main.yml) runs
  `check release` with Tagger's calculated version; root Tagger releases master
  and enables GitHub releases. Inspect the plugin-provided release task graph
  before modifying wiring; the direct CLI build script does not establish all
  release dependencies or failure propagation.
- [CPL-33](CPL-33-05_CDN_LOOKUP_COUPLING_MIGRATION_AND_RELEASE.md) concerns a
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
- [ ] Two otherwise identical builds at distinct release versions compare equal;
  changed runtime contents/metadata compare different. Record the actual packed
  layout and all supported exclusions.
- [ ] Controlled task-boundary evidence shows equality invokes no npm publication,
  changes publish the compared artifact, errors prevent publication, and snapshots
  retain their existing behavior. Cover both public CLI publication task paths.
- [ ] Confirm application release, deployment, JVM and S3 wiring is unaffected by
  npm equality; use task-graph/composition evidence rather than source assertions.
- [ ] Record focused commands, full `./gradlew check` results and slice commits.
  Follow the documented external-sandbox/fish Gradle execution guidance. Full
  checks alone cannot prove npm comparison, so retain the bounded evidence above.
- [ ] Document the local non-publishing command and intentional application/npm
  version gaps. Any live CI/npm observation requires separate push/release
  authorization; record it if available, otherwise label it unverified without
  inventing a follow-up monitoring system.
- [ ] Review overall outcome, update relevant links, and archive when the required
  implementation evidence is complete under [work-card rules](../context/WORK_CHECKLIST.md).

## Current evidence and next action

- Direct `./gradlew` works; no fish wrapper is needed in this environment.
- `./gradlew :cli:jsCliTar --no-daemon` and
  `./gradlew check release --dry-run --no-daemon` passed. The latter shows
  application tag/release and deployment before CLI npm publication, followed by
  GitHub release finalization. Preserve that existing ordering.
- Actual `npm pack` of the generated project contains 1,890 files (8,183,505
  unpacked bytes), including compiled shared modules and source maps. No bundled
  node_modules; runtime dependencies are ordinary manifest dependencies.
- Isolated installation under `/tmp`, invoked from `/tmp`, runs on repository
  Node 26.13.0 with the existing launcher. Manifest version is
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
  No changes to JVM distribution, S3 artifact, workflow or application versioning.
- Slice 1 full `./gradlew check --console=plain` passed in 2m 56s (699 actionable
  tasks), including SDK integration, real-browser and E2E checks. Complete slice
  review and `git diff --check` passed; no push or release performed.
