package com.zegreatrob.coupling.plugins

import org.gradle.api.Action
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

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

    fun applyCommonDependencies(project: Project) {
        project.dependencies.apply {
            val kotlinExt = project.extensions.findByType(KotlinMultiplatformExtension::class.java)
            if (kotlinExt != null) {
                add("commonMainApi", project.dependencies.enforcedPlatform(project.project(":libraries:dependency-bom")))
                add("commonMainImplementation", "org.jetbrains.kotlinx:kotlinx-serialization-core")
                add("commonMainImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core")
            } else {
                add("api", project.dependencies.enforcedPlatform(project.project(":libraries:dependency-bom")))
                add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-core")
                add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core")
            }
        }
    }
}
