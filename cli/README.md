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
