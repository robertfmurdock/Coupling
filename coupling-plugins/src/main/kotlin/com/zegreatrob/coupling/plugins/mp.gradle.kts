package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

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
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    jvmToolchain(22)
    compilerOptions {
        allWarningsAsErrors = true
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.uuid.ExperimentalUuidApi")
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

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
