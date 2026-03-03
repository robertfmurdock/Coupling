@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    alias(libs.plugins.io.github.turansky.seskar)
}

val COMMON_FREE_COMPILER_ARGS = listOf(
    "-Xexpect-actual-classes",
    "-Xdont-warn-on-error-suppression",

    "-Xwarning-level=NOTHING_TO_INLINE:disabled",
)

val COMMON_OPT_INS = listOf(
    "kotlin.ExperimentalStdlibApi",
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.ExperimentalMultiplatform",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.js.ExperimentalJsExport",
)

val COMMON_INTERNAL_OPT_INS = listOf(
    "js.internal.InternalApi",
    "kotlin.js.ExperimentalWasmJsInterop",
)

val JS_FREE_COMPILER_ARGS = listOf(
    "-Xes-long-as-bigint",
    "-Xir-generate-inline-anonymous-functions",
)

kotlin {
    applyDefaultHierarchyTemplate()




    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.addAll(COMMON_FREE_COMPILER_ARGS)
        optIn.addAll(COMMON_OPT_INS)

        if (project.name != "kotlin-css") {
            optIn.addAll(COMMON_INTERNAL_OPT_INS)
        }
    }


    js {
        configureJsTarget()
    }

    sourceSets {
        jsMain {
            dependencies {
                implementation(devNpm("css-loader", "7.1.2"))
                implementation(devNpm("style-loader", "4.0.0"))
            }
        }
    }
    sourceSets.webMain {
        kotlin.srcDir(projectDir.resolve("src/webMain/generated"))
    }

    sourceSets.jsMain {
        kotlin.srcDir(projectDir.resolve("src/jsMain/generated"))
    }
}

fun KotlinJsTargetDsl.configureJsTarget() {
    outputModuleName = project.name

    nodejs()

    compilerOptions {
        target = "es2015"

        freeCompilerArgs.addAll(JS_FREE_COMPILER_ARGS)
    }
}

dependencies {
    "webMainApi"("org.jetbrains.kotlin-wrappers:kotlin-browser")
    "webMainApi"(npmConstrained("react-router-dom"))
    "jsMainApi"("org.jetbrains.kotlin-wrappers:kotlin-react")
    "jsMainApi"("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
}
