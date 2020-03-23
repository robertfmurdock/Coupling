package com.zegreatrob.coupling.build

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import java.io.File


fun Project.loadPackageJson(): PackageJson {
    val packageJsonPath = "${projectDir.path}/package.json"
    return PackageJson(ObjectMapper().readTree(File(packageJsonPath)))
}


data class PackageJson(val json: JsonNode) {

    fun dependencies() = json["dependencies"].dependencyEntries()
    fun devDependencies() = json["devDependencies"].dependencyEntries()

    private fun JsonNode.dependencyEntries() = fields().asSequence().map { entry ->
        entry.key to entry.value
    }

}