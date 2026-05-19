package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.conventions.KotlinConventions
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

version = "0.0.0"

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    KotlinConventions.applyStrictCompilation(this)
    compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }
    KotlinConventions.applyCommonOptIns(this)
}

KotlinConventions.applyCommonDependencies(project)

tasks.withType(KotlinJsTest::class).configureEach {
    outputs.cacheIf { true }
}
