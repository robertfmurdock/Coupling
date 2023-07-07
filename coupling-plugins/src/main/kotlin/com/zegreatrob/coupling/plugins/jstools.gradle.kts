package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("js")
    id("com.zegreatrob.jsmints.plugins.jspackage")
    id("com.zegreatrob.jsmints.plugins.ncu")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
//    id("com.zegreatrob.testmints.logs.mint-logs")
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

project.extensions.create<JsConstraintExtension>("npmConstrained")
configure<JsConstraintExtension> {
    json = File(project(":libraries:js-dependencies").projectDir, "package.json")
}

dependencies {
    implementation(enforcedPlatform(project(":libraries:dependency-bom")))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}
