package com.zegreatrob.coupling.plugins

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmDependencyExtension

fun DependencyHandlerScope.npmConstrained(name: String, jsConstraintExtension: JsConstraintExtension): Dependency {
    return jsConstraintExtension
        .dependencies()!!
        .first { (key, _) -> key == name }
        .let { npm(name, it.second.asText()) }
}

val DependencyHandler.npm: NpmDependencyExtension get() =
    (this as ExtensionAware).extensions.getByName("npm") as NpmDependencyExtension
