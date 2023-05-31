package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.plugin.mpp.MetadataDependencyTransformationTask
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    kotlin("jvm")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

version = "0.0.0"

kotlin {
    target {
        compilations.configureEach {
            kotlinOptions {
                allWarningsAsErrors = true
            }
        }
    }
    jvmToolchain(17)
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
    api(enforcedPlatform(project(":libraries:dependency-bom")))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType(KotlinJvmTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(MetadataDependencyTransformationTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.jvm.tasks.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.language.jvm.tasks.ProcessResources::class).configureEach {
    outputs.cacheIf { true }
}
