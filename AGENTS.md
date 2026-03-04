# AGENTS

This repo is a multi-module Gradle project (Kotlin/JVM and Kotlin/JS) that
includes a web client and a server.

## Quick commands
- `./gradlew test`
- `./gradlew build`
- `./gradlew :module:task` (run module-specific tasks as needed)

## Layout
- `client/` frontend (Kotlin/JS, Vite config in `client/vite.config.mjs`)
- `server/` backend services
- `libraries/` shared modules
- `cli/`, `sdk/`, `e2e/`, `scripts/`, `deploy/` supporting tooling

## Notes for agents
- Use the Gradle wrapper (`./gradlew`) for builds and tests.
- Keep changes small and consistent with existing patterns.
