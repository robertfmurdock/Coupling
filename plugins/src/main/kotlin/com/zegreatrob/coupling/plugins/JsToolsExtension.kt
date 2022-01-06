package com.zegreatrob.coupling.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.api.Project
import java.io.File

abstract class JsToolsExtension(val packageJson: PackageJson)

fun Project.loadPackageJson(): PackageJson {
    val packageJsonPath = "${projectDir.path}/package.json"
    return PackageJson(ObjectMapper().readTree(File(packageJsonPath)))
}