package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.conventions.KotlinConventions
import com.zegreatrob.coupling.plugins.js.JsConstraintExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("multiplatform")
    kotlin("plugin.js-plain-objects")
    id("com.zegreatrob.jsmints.plugins.jspackage")
    id("com.zegreatrob.jsmints.plugins.ncu")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

kotlin {
    KotlinConventions.applyStrictCompilation(this)
    js {
        useEsModules()
        compilerOptions { target = "es2015" }
        binaries.executable()
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }
    KotlinConventions.applyCommonOptIns(this)
}

version = "0.0.0"

project.extensions.create<JsConstraintExtension>("npmConstrained")
configure<JsConstraintExtension> {
    json = File(project(":libraries:js-dependencies").projectDir, "package.json")
}

dependencies {
    jsMainImplementation(enforcedPlatform(project(":libraries:dependency-bom")))
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
}

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}

afterEvaluate {
    project.extensions.findByType(NodeJsEnvSpec::class.java).let {
        it?.version = "23.9.0"
    }
}
