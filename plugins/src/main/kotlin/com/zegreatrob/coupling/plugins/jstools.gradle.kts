package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("js")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
}

val toolsExtension = project.extensions.create("jstools", JsToolsExtension::class, loadPackageJson())

kotlin {
    js {
        useCommonJs()
        binaries.executable()
    }
}

dependencies {
    toolsExtension.packageJson.dependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    toolsExtension.packageJson.devDependencies()?.forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.330-kotlin-1.6.20"))
    implementation(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:7.2.5"))
    implementation(enforcedPlatform("com.zegreatrob.jsmints:jsmints-bom:1.1.3"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.2"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.1"))
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
tasks.withType(ProcessResources::class).configureEach {
    outputs.cacheIf { true }
}
