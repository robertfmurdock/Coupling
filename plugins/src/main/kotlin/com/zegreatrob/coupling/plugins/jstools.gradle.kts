package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("js")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.jsmints.plugins.jspackage")
    id("org.jlleitschuh.gradle.ktlint")
}

kotlin {
    js {
        useCommonJs()
        binaries.executable()
    }
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    implementation(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:8.0.3"))
    implementation(enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:1.5.16"))
    implementation(enforcedPlatform("io.ktor:ktor-bom:2.0.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.348"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.3"))
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
