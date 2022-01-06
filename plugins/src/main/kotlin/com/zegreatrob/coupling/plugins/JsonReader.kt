package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import java.io.File

data class PackageJson(val json: JsonNode) {

    fun dependencies() = json["dependencies"]?.dependencyEntries()
    fun devDependencies() = json["devDependencies"]?.dependencyEntries()

    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }

}
