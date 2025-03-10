@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
}

kotlin {
    js { nodejs { testTask { useMocha { timeout = "10s" } } } }
    jvm()
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }
}

dependencies {
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":libraries:logging"))
    commonMainApi("com.zegreatrob.testmints:action")
    commonMainApi("com.zegreatrob.testmints:action-async")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("com.zegreatrob.testmints:action-annotation")

    commonTestImplementation(project(":libraries:json"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:async")
    commonTestImplementation("com.zegreatrob.testmints:minassert")

    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")


    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.slf4j:slf4j-simple")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    named<Test>("jvmTest") {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
    "formatKotlinCommonMain" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    "formatKotlinJsMain" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    "formatKotlinJsTest" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    "lintKotlinCommonMain" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    "lintKotlinJsMain" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    "lintKotlinJsTest" {
        dependsOn("kspCommonMainKotlinMetadata")
    }
    withType(KotlinCompilationTask::class).configureEach {
        if (name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}

afterEvaluate {
    dependencies {
        configurations.kspCommonMainMetadata(enforcedPlatform(project(":libraries:dependency-bom")))
        configurations.kspCommonMainMetadata("com.zegreatrob.testmints:action-processor")
    }
}
