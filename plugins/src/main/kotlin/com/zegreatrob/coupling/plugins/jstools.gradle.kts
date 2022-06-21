package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("js")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("org.jlleitschuh.gradle.ktlint")
}

val toolsExtension = project.extensions.create("jstools", JsToolsExtension::class, loadPackageJson())

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
    toolsExtension.packageJson.dependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    toolsExtension.packageJson.devDependencies()?.forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.346"))
    implementation(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:8.0.2"))
    implementation(enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:1.5.4"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.3"))
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(org.gradle.api.tasks.bundling.Jar::class).configureEach {
    outputs.cacheIf { true }
}
