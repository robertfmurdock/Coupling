package com.zegreatrob.coupling.plugins.js

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.nio.file.Files
import java.util.zip.GZIPOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NpmPublicationTaskTest {
    @Test
    fun `identical package skips npm publication through either entry point`() {
        listOf("jsPublish", "publish").forEach { entryPoint ->
            val directory = Files.createTempDirectory("npm-publication-test").toFile().canonicalFile
            try {
                publicationFixture(directory)
                val result = GradleRunner.create().withProjectDir(directory).withPluginClasspath()
                    .withArguments(entryPoint, "--stacktrace", "--configuration-cache").build()

                val commands = directory.resolve("commands.txt").readLines()
                assertEquals(false, commands.any { it.startsWith("publish ") }, result.output)
                assertEquals(1, commands.count { it.startsWith("view ") }, result.output)
                assertEquals(1, commands.count { it.startsWith("pack ") }, result.output)
            } finally {
                directory.deleteRecursively()
            }
        }
    }

    @Test
    fun `changed package publishes the compared tarball through either entry point`() {
        listOf("jsPublish", "publish").forEach { entryPoint ->
            withPublicationFixture { directory ->
                writePackage(directory.resolve("candidate.tgz"), "1.1.0", mapOf("kotlin/main.mjs" to (493 to "changed shared code")))

                runner(directory).withArguments(entryPoint, "--configuration-cache").build()

                val commands = directory.resolve("commands.txt").readLines()
                assertEquals("publish ${directory.resolve("candidate.tgz").absolutePath}", commands.last())
                assertEquals(1, commands.count { it.startsWith("view ") })
                assertEquals(1, commands.count { it.startsWith("pack @continuous-excellence/coupling-cli@1.0.0 ") })
                assertEquals(
                    setOf(directory.resolve("npm-context").absolutePath),
                    directory.resolve("working-directories.txt").readLines().toSet(),
                )
            }
        }
    }

    @Test
    fun `registry and archive failures prevent publication through either entry point`() {
        val failures: Map<String, (File) -> Unit> = mapOf(
            "registry lookup failure" to { it.resolve("fail-view").writeText("") },
            "missing latest" to { it.resolve("latest.json").writeText("null") },
            "invalid latest metadata" to { it.resolve("latest.json").writeText("not-json") },
            "invalid latest version" to { it.resolve("latest.json").writeText("\"1.0.0-..\"") },
            "baseline fetch failure" to { it.resolve("fail-pack").writeText("") },
            "invalid archive" to { it.resolve("baseline.tgz").writeText("not-an-archive") },
            "wrong exact baseline" to { writePackage(it.resolve("baseline.tgz"), "0.9.0") },
        )
        listOf("jsPublish", "publish").forEach { entryPoint ->
            failures.forEach { (scenario, arrange) ->
                withPublicationFixture { directory ->
                    arrange(directory)

                    val result = runner(directory).withArguments(entryPoint, "--configuration-cache").buildAndFail()

                    val commands = directory.resolve("commands.txt").readLines()
                    assertEquals(false, commands.any { it.startsWith("publish ") }, "$entryPoint: $scenario\n${result.output}")
                }
            }
        }
    }

    @Test
    fun `snapshots skip baseline lookup and publication through either entry point`() {
        listOf("jsPublish", "publish").forEach { entryPoint ->
            withPublicationFixture { directory ->
                val result = runner(directory).withArguments(entryPoint, "-Psnapshot=true", "--configuration-cache").build()

                assertEquals(TaskOutcome.SKIPPED, result.task(":jsPublish")?.outcome)
                assertEquals(false, directory.resolve("commands.txt").exists())
            }
        }
    }

    @Test
    fun `unchanged candidate rechecks a fresh exact latest with configuration cache reused`() {
        withPublicationFixture { directory ->
            runner(directory).withArguments("jsPublish", "--configuration-cache").build()
            directory.resolve("latest.json").writeText("\"1.0.1\"")
            writePackage(directory.resolve("baseline.tgz"), "1.0.1", mapOf("kotlin/main.mjs" to (420 to "different registry contents")))

            val result = runner(directory).withArguments("jsPublish", "--configuration-cache").build()

            val commands = directory.resolve("commands.txt").readLines()
            assertTrue(result.output.contains("Reusing configuration cache."), result.output)
            assertEquals(2, commands.count { it.startsWith("view ") })
            assertEquals(1, commands.count { it.startsWith("pack @continuous-excellence/coupling-cli@1.0.1 ") })
            assertEquals("publish ${directory.resolve("candidate.tgz").absolutePath}", commands.last())
        }
    }

    @Test
    fun `comparison command reports changes without publishing`() {
        withPublicationFixture { directory ->
            writePackage(directory.resolve("candidate.tgz"), "1.1.0", mapOf("README.md" to (420 to "new documentation")))

            val result = runner(directory).withArguments("compareNpmPackage", "--configuration-cache").build()

            assertTrue(result.output.contains("npm package changed"), result.output)
            assertEquals(false, directory.resolve("commands.txt").readLines().any { it.startsWith("publish ") })
        }
    }

    @Test
    fun `npm publication failures propagate to the public task`() {
        withPublicationFixture { directory ->
            directory.resolve("fail-publish").writeText("")
            writePackage(directory.resolve("candidate.tgz"), "1.1.0", mapOf("README.md" to (420 to "new documentation")))

            val result = runner(directory).withArguments("publish", "--configuration-cache").buildAndFail()

            assertEquals(TaskOutcome.FAILED, result.task(":jsPublish")?.outcome)
            assertTrue(result.output.contains("npm publish failed"), result.output)
        }
    }

    @Test
    fun `npm equality leaves application delivery JVM distribution and S3 finalizers running`() {
        withPublicationFixture { directory ->
            val result = runner(directory).withArguments("release", "--configuration-cache").build()

            listOf(":release", ":deploy:prod:deploy", ":distTar", ":uploadToS3", ":jsPublish").forEach { path ->
                assertEquals(TaskOutcome.SUCCESS, result.task(path)?.outcome, result.output)
            }
            assertEquals(false, directory.resolve("commands.txt").readLines().any { it.startsWith("publish ") })
        }
    }
}

private fun runner(directory: File): GradleRunner = GradleRunner.create().withProjectDir(directory).withPluginClasspath()

private fun withPublicationFixture(test: (File) -> Unit) {
    val directory = Files.createTempDirectory("npm-publication-test").toFile().canonicalFile
    try {
        publicationFixture(directory)
        test(directory)
    } finally {
        directory.deleteRecursively()
    }
}

private fun publicationFixture(directory: File) {
    directory.resolve("npm-context").mkdirs()
    directory.resolve("settings.gradle.kts").writeText("rootProject.name = \"npm-publication-fixture\"\ninclude(\":deploy:prod\")")
    directory.resolve("deploy/prod").mkdirs()
    directory.resolve("deploy/prod/build.gradle.kts").writeText("tasks.register(\"deploy\") { doLast { println(\"fixture:deploy\") } }")
    directory.resolve("build.gradle.kts").writeText(
        """
        import com.zegreatrob.coupling.plugins.js.*
        plugins { id("com.zegreatrob.coupling.plugins.linter") }
        tasks.register("check")
        tasks.register("distTar") { doLast { println("fixture:distTar") } }
        tasks.register("uploadToS3") { doLast { println("fixture:uploadToS3") } }
        tasks.register("release") {
            dependsOn("distTar", ":deploy:prod:deploy")
            finalizedBy("publish", "uploadToS3")
            doLast { println("fixture:release") }
        }
        val pack = tasks.register<NpmPackTask>("npmPack") {
            enabled = false
            archiveFile.set(layout.projectDirectory.file("candidate.tgz"))
            packageDirectory.set(layout.projectDirectory.dir("npm-context"))
        }
        val verification = tasks.register<VerifyNpmPackageTask>("verifyNpmPackage") { enabled = false }
        registerCliNpmPublication(pack, verification, providers.gradleProperty("snapshot").isPresent)
        tasks.withType<NpmPublishTask>().configureEach {
            npmCommand.set(listOf("sh", layout.projectDirectory.file("npm-fixture.sh").asFile.absolutePath))
        }
        """.trimIndent(),
    )
    directory.resolve("latest.json").writeText("\"1.0.0\"")
    writePackage(directory.resolve("candidate.tgz"), "1.1.0")
    writePackage(directory.resolve("baseline.tgz"), "1.0.0")
    directory.resolve("npm-fixture.sh").writeText(
        """
        set -eu
        printf '%s\n' "${'$'}PWD" >> "${'$'}(dirname "${'$'}0")/working-directories.txt"
        cd "${'$'}(dirname "${'$'}0")"
        printf '%s\n' "${'$'}*" >> commands.txt
        if [ -f "fail-${'$'}1" ]; then exit 23; fi
        case "${'$'}1" in
          view) cat latest.json ;;
          pack)
            for argument in "${'$'}@"; do destination="${'$'}argument"; done
            cp baseline.tgz "${'$'}destination/baseline.tgz"
            printf '[{"filename":"baseline.tgz"}]'
            ;;
          publish) ;;
          *) exit 42 ;;
        esac
        """.trimIndent(),
    )
}

internal fun writePackage(
    file: File,
    version: String,
    files: Map<String, Pair<Int, String>> = mapOf("kotlin/main.mjs" to (420 to "console.log('hello')")),
) {
    val manifest = """{"name":"@continuous-excellence/coupling-cli","version":"$version","bin":{"coupling":"kotlin/main.mjs"}}"""
    TarArchiveOutputStream(GZIPOutputStream(file.outputStream())).use { tar ->
        (mapOf("package.json" to (420 to manifest)) + files).forEach { (path, contents) ->
            val bytes = contents.second.toByteArray()
            val entry = TarArchiveEntry("package/$path").apply {
                size = bytes.size.toLong()
                mode = contents.first
            }
            tar.putArchiveEntry(entry)
            tar.write(bytes)
            tar.closeArchiveEntry()
        }
    }
}
