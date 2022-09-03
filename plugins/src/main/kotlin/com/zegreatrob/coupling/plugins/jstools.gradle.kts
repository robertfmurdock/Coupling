package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("js")
    id("com.zegreatrob.jsmints.plugins.jspackage")
    id("com.zegreatrob.jsmints.plugins.ncu")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    js {
        useCommonJs()
        binaries.executable()
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
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
}

version = "0.0.0"

ktlint {
    version.set("0.45.2")
}

dependencies {
    implementation(enforcedPlatform(project(":coupling-libraries:dependency-bom")))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.targets.js.npm.PublicPackageJsonTask::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.language.jvm.tasks.ProcessResources::class).configureEach {
    outputs.cacheIf { true }
}
