package com.zegreatrob.coupling.plugins.js

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import java.io.File
import java.nio.file.Files
import java.util.zip.GZIPOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class NpmPackageContentsTest {
    @Test
    fun `rejects malformed semantic versions before comparing packages`() {
        listOf("01.0.0", "1.0.0-..", "1.0.0-01", "1.0.0+", "latest").forEach { version ->
            withArchives { _, candidate ->
                archive(candidate, listOf(Entry("package/package.json", manifest(version))))

                assertFails(version) { readNpmPackage(candidate) }
            }
        }
    }

    @Test
    fun `ignores release version archive ordering ownership timestamps and non-executable modes`() = withArchives { baseline, candidate ->
        archive(
            baseline,
            listOf(
                Entry("package/", mode = 493),
                Entry("package/package.json", manifest("1.0.0")),
                Entry("package/kotlin/main.mjs", "compiled shared code", 493),
            ),
        )
        archive(
            candidate,
            listOf(
                Entry("package/kotlin/main.mjs", "compiled shared code", 365),
                Entry("package/package.json", """{ "version":"2.0.0", "name":"@continuous-excellence/coupling-cli" }""", 384),
            ),
            timestamp = 123456000L,
            owner = 42,
        )

        val changes = readNpmPackage(candidate).changedPaths(readNpmPackage(baseline))

        assertEquals(emptyList(), changes)
    }

    @Test
    fun `counts compiled code shared modules source maps documentation and executable bits`() {
        val changes = mapOf(
            "compiled code" to Entry("package/kotlin/main.mjs", "console.log('2.0.0')", 493),
            "shared module" to Entry("package/kotlin/shared.mjs", "changed model"),
            "source map" to Entry("package/kotlin/main.mjs.map", "changed source map"),
            "documentation" to Entry("package/README.md", "changed instructions"),
            "license" to Entry("package/LICENSE", "changed license"),
            "executable bits" to Entry("package/kotlin/main.mjs", "console.log('1.0.0')", 420),
        )
        changes.forEach { (scenario, changed) ->
            withArchives { baseline, candidate ->
                val contents = listOf(
                    Entry("package/package.json", manifest("1.0.0")),
                    Entry("package/kotlin/main.mjs", "console.log('1.0.0')", 493),
                    Entry("package/kotlin/shared.mjs", "original model"),
                    Entry("package/kotlin/main.mjs.map", "original source map"),
                    Entry("package/README.md", "original instructions"),
                    Entry("package/LICENSE", "original license"),
                )
                archive(baseline, contents)
                archive(candidate, contents.map { if (it.path == changed.path) changed else it })

                val paths = readNpmPackage(candidate).changedPaths(readNpmPackage(baseline))

                assertEquals(listOf(changed.path.removePrefix("package/")), paths, scenario)
            }
        }
    }

    @Test
    fun `counts added and deleted files`() = withArchives { baseline, candidate ->
        writePackage(baseline, "1.0.0", mapOf("removed.mjs" to (420 to "old")))
        writePackage(candidate, "2.0.0", mapOf("added.mjs" to (420 to "new")))

        val changes = readNpmPackage(candidate).changedPaths(readNpmPackage(baseline))

        assertEquals(listOf("added.mjs", "removed.mjs"), changes)
    }

    @Test
    fun `counts all manifest fields except top-level version`() {
        val metadata = mapOf(
            "runtime dependencies" to """"dependencies":{"open":"^12.0.0"}""",
            "development dependencies" to """"devDependencies":{"source-map-support":"0.6.0"}""",
            "bin" to """"bin":{"coupling":"different.mjs"}""",
            "main" to """"main":"different.mjs"""",
            "exports" to """"exports":{".":"./different.mjs"}""",
            "engines" to """"engines":{"node":">=26"}""",
            "nested version" to """"custom":{"version":"2.0.0"}""",
        )
        metadata.forEach { (scenario, field) ->
            withArchives { baseline, candidate ->
                archive(baseline, listOf(Entry("package/package.json", manifest("1.0.0"))))
                archive(candidate, listOf(Entry("package/package.json", manifest("2.0.0").dropLast(1) + ",$field}")))

                val changes = readNpmPackage(candidate).changedPaths(readNpmPackage(baseline))

                assertEquals(listOf("package.json"), changes, scenario)
            }
        }
    }

    @Test
    fun `rejects unsafe paths duplicate entries links and invalid manifests`() {
        val invalidEntries = mapOf(
            "traversal" to listOf(Entry("package/../outside", "bad")),
            "absolute path" to listOf(Entry("/outside", "bad")),
            "backslash" to listOf(Entry("package/a\\b", "bad")),
            "duplicate files" to listOf(Entry("package/a", "one"), Entry("package/a", "two")),
            "duplicate directories" to listOf(Entry("package/"), Entry("package/")),
            "symlink" to listOf(Entry("package/link", type = '2')),
            "hardlink" to listOf(Entry("package/link", type = '1')),
            "missing manifest" to listOf(Entry("package/file", "hello")),
            "invalid JSON" to listOf(Entry("package/package.json", "not-json")),
            "duplicate JSON fields" to listOf(Entry("package/package.json", """{"name":"cli","version":"1.0.0","version":"2.0.0"}""")),
            "trailing JSON" to listOf(Entry("package/package.json", manifest("1.0.0") + "{}")),
            "invalid version" to listOf(Entry("package/package.json", manifest("latest"))),
        )
        invalidEntries.forEach { (scenario, entries) ->
            withArchives { _, candidate ->
                archive(candidate, entries)

                assertFails(scenario) { readNpmPackage(candidate) }
            }
        }
    }

    @Test
    fun `rejects truncated archives and corrupt gzip checksums`() = withArchives { baseline, candidate ->
        writePackage(baseline, "1.0.0")
        val bytes = baseline.readBytes()
        candidate.writeBytes(bytes.copyOf(bytes.size / 2))
        assertFails { readNpmPackage(candidate) }
        bytes[bytes.lastIndex - 7] = (bytes[bytes.lastIndex - 7].toInt() xor 1).toByte()
        candidate.writeBytes(bytes)
        assertFails { readNpmPackage(candidate) }
    }
}

private fun manifest(version: String) = """{"name":"@continuous-excellence/coupling-cli","version":"$version"}"""

private data class Entry(val path: String, val contents: String = "", val mode: Int = 420, val type: Char = '0')

private fun archive(file: File, entries: List<Entry>, timestamp: Long = 0L, owner: Long = 0L) {
    TarArchiveOutputStream(GZIPOutputStream(file.outputStream())).use { tar ->
        entries.forEach { data ->
            val bytes = data.contents.toByteArray()
            val entry = TarArchiveEntry(data.path, data.type.code.toByte(), true).apply {
                size = bytes.size.toLong()
                mode = data.mode
                setModTime(timestamp)
                setUserId(owner)
                setGroupId(owner)
            }
            tar.putArchiveEntry(entry)
            tar.write(bytes)
            tar.closeArchiveEntry()
        }
    }
}

private fun withArchives(test: (File, File) -> Unit) {
    val directory = Files.createTempDirectory("npm-package-test").toFile()
    try {
        test(directory.resolve("baseline.tgz"), directory.resolve("candidate.tgz"))
    } finally {
        directory.deleteRecursively()
    }
}
