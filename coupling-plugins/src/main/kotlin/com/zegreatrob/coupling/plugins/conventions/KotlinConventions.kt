package com.zegreatrob.coupling.plugins.conventions

import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin

object KotlinConventions {
    fun applyCommonOptIns(kotlinExtension: KotlinProjectExtension) {
        kotlinExtension.sourceSets.all {
            languageSettings.apply {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.uuid.ExperimentalUuidApi")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }

    fun applyStrictCompilation(kotlinExtension: KotlinJvmProjectExtension) {
        kotlinExtension.jvmToolchain(22)
        kotlinExtension.compilerOptions.apply {
            allWarningsAsErrors.set(true)
        }
    }

    fun applyStrictCompilation(kotlinExtension: KotlinMultiplatformExtension) {
        kotlinExtension.jvmToolchain(22)
        kotlinExtension.compilerOptions.apply {
            allWarningsAsErrors.set(true)
        }
    }

    fun configureNodeRuntime(project: Project) {
        val root = project.rootProject
        val versionFile = root.layout.projectDirectory.file(".node-version")
        val nodeVersion = project.providers.fileContents(versionFile).asText.map(String::trim)
        val useSystemNode = project.providers.environmentVariable("CI").isPresent

        listOf(root, project).forEach { nodeProject ->
            nodeProject.plugins.withType(NodeJsPlugin::class.java) {
                val node = nodeProject.extensions.getByType(NodeJsEnvSpec::class.java)
                node.version.set(nodeVersion)
                if (useSystemNode) {
                    node.download.set(false)
                }
            }
        }
    }

    fun applyCommonDependencies(project: Project) {
        project.dependencies.apply {
            val kotlinExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kotlinExt != null) {
                add("commonMainApi", project.dependencies.enforcedPlatform(project.dependencies.project(":libraries:dependency-bom")))
                add("commonMainImplementation", "org.jetbrains.kotlinx:kotlinx-serialization-core")
                add("commonMainImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core")
            } else {
                add("api", project.dependencies.enforcedPlatform(project.dependencies.project(":libraries:dependency-bom")))
                add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-core")
                add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
    }
}
