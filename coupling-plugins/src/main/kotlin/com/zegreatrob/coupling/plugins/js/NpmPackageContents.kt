package com.zegreatrob.coupling.plugins.js

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest
import java.util.HexFormat
import java.util.zip.GZIPInputStream

internal data class NpmFile(val digest: String, val executableBits: Int)

internal data class NpmPackageContents(val files: Map<String, NpmFile>, val manifest: ObjectNode) {
    val name: String get() = manifest["name"].asText()
    val version: String get() = manifest["version"].asText()

    fun changedPaths(other: NpmPackageContents): List<String> = (files.keys + other.files.keys).sorted().filter { path ->
        if (path == "package.json") {
            manifest.withoutVersion() != other.manifest.withoutVersion() ||
                files[path]?.executableBits != other.files[path]?.executableBits
        } else {
            files[path] != other.files[path]
        }
    }
}

private fun ObjectNode.withoutVersion(): ObjectNode = deepCopy().apply { remove("version") }

internal fun npmJsonMapper(): ObjectMapper = ObjectMapper()
    .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
    .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)

internal fun readNpmPackage(file: File): NpmPackageContents {
    val files = mutableMapOf<String, NpmFile>()
    val paths = mutableSetOf<String>()
    var manifest: ObjectNode? = null
    GZIPInputStream(file.inputStream()).use { gzip ->
        TarArchiveInputStream(gzip).use { tar ->
            generateSequence { tar.nextEntry }.forEach { entry ->
                val path = validateEntry(entry, paths)
                if (!entry.isDirectory) {
                    val bytes = tar.readBytes()
                    if (path == "package.json") manifest = readManifest(bytes)
                    files[path] = NpmFile(sha256(bytes), entry.mode and 0b001001001)
                }
            }
            // The tar EOF can precede the gzip trailer; drain it to validate the CRC.
            gzip.transferTo(OutputStream.nullOutputStream())
        }
    }
    return NpmPackageContents(files, requireNotNull(manifest) { "Missing package/package.json in $file" })
}

private fun validateEntry(entry: TarArchiveEntry, paths: MutableSet<String>): String {
    val path = entry.name.removeSuffix("/")
    require(entry.isCheckSumOK) { "Invalid tar checksum: $path" }
    require((entry.isFile || entry.isDirectory) && !entry.isSparse) { "Unsupported npm archive entry: $path" }
    require(path.startsWith("package/") || (entry.isDirectory && path == "package")) { "Invalid npm archive path: $path" }
    require('\\' !in path && path.split('/').none { it.isEmpty() || it == "." || it == ".." }) {
        "Unsafe npm archive path: $path"
    }
    require(paths.add(path)) { "Duplicate npm archive path: $path" }
    return path.removePrefix("package/")
}

private fun readManifest(bytes: ByteArray): ObjectNode {
    val manifest = npmJsonMapper().readTree(bytes)
    require(manifest is ObjectNode) { "npm package manifest must be an object" }
    require(manifest["name"]?.isTextual == true && manifest["name"].asText().isNotBlank()) { "Invalid npm package name" }
    require(manifest["version"]?.isTextual == true && validNpmVersion(manifest["version"].asText())) { "Invalid npm package version" }
    return manifest
}

internal fun validNpmVersion(version: String): Boolean {
    val core = "(0|[1-9][0-9]*)"
    val identifiers = "[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*"
    val match = Regex("$core\\.$core\\.$core(?:-($identifiers))?(?:\\+$identifiers)?").matchEntire(version) ?: return false
    return match.groupValues[4].split('.').none { identifier ->
        identifier.length > 1 && identifier.startsWith('0') && identifier.all(Char::isDigit)
    }
}

private fun sha256(bytes: ByteArray): String = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))
