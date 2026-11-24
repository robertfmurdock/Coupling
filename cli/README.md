# CLI packaging

`./gradlew :cli:verifyNpmPackage` produces a real npm tarball at
`cli/build/npm/coupling-cli.tgz`, installs it in an isolated consumer, and checks
`coupling --version` and `coupling --help` from an unrelated working directory
without application credentials. This verification also runs in `check` and
before `:cli:publish` or `:cli:jsPublish`. Publication uses that exact tarball.

The installed JS CLI reads its package manifest version, independently of the
working directory and `COUPLING_VERSION`. JVM distributions retain the generated
application version. The separate `jsCliTar` distribution and S3 upload remain
unchanged.

## Comparing with npm

`./gradlew :cli:compareNpmPackage` verifies the packed consumer, resolves npm
`latest` once, fetches that exact version, and reports equality or changed paths.
It never publishes. Registry, metadata, fetch, and archive errors fail the task;
there is no automatic first-publication exception. npm uses the existing registry
and authentication configuration.

`:cli:publish` and `:cli:jsPublish` perform that comparison afresh on every attempt.
Equal packages succeed without invoking `npm publish`; changed packages publish
the verified and compared tarball. Snapshots retain their publication exclusion.
The registry decision is never reused from Gradle's build or configuration cache.

Equality compares package-relative file paths, contents, and executable bits.
Manifest JSON is compared structurally, ignoring only top-level `version`.
Dependencies (including development dependencies), executable metadata,
documentation, compiled shared modules, and source maps count. Archive ordering,
timestamps, ownership, directory entries, and non-executable mode bits do not.

Application versions, Git tags/releases, deployments, JVM distribution and S3
uploads remain independent of npm equality. npm versions may lag application
versions or have gaps. Existing publication ordering remains: an npm failure does
not undo an application tag or deployment that already completed.

The CLI production linker currently keeps `kotlinx.coroutines.channels.Channel`
and disables member-name minification. Kotlin 2.4.21 otherwise emits unstable
interface order when DCE expands this removed interface; keeping it with minified
member names also produces mismatched iterator calls. Re-evaluate this scoped
workaround when upgrading Kotlin, using independently linked tarballs and the
packed-consumer check. It adds approximately 97 KB to the compressed package.
