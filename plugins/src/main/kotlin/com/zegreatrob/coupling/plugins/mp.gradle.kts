package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:7.3.2"))
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:1.1.5"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.2"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.333"))
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(KotlinJvmTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.jetbrains.kotlin.gradle.plugin.mpp.TransformKotlinGranularMetadata::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.jvm.tasks.Jar::class).configureEach {
    outputs.cacheIf { true }
}


