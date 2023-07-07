package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyTransformationTask
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

version = "0.0.0"

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
}

dependencies {
    commonMainApi(enforcedPlatform(project(":libraries:dependency-bom")))
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
