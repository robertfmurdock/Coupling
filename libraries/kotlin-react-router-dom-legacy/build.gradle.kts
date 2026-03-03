@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    alias(libs.plugins.io.github.turansky.seskar)
}

kotlin {
    applyDefaultHierarchyTemplate()

    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.addAll(
            listOf(
                "-Xexpect-actual-classes",
                "-Xdont-warn-on-error-suppression",

                "-Xwarning-level=NOTHING_TO_INLINE:disabled",
            )
        )
        optIn.addAll(
            listOf(
                "kotlin.ExperimentalStdlibApi",
                "kotlin.ExperimentalUnsignedTypes",
                "kotlin.ExperimentalMultiplatform",
                "kotlin.contracts.ExperimentalContracts",
                "kotlin.js.ExperimentalJsExport",
            )
        )
        optIn.addAll(
            listOf(
                "js.internal.InternalApi",
                "kotlin.js.ExperimentalWasmJsInterop",
            )
        )
    }


    js {
        outputModuleName = project.name

        nodejs()

        compilerOptions {
            target = "es2015"

            freeCompilerArgs.addAll(
                listOf(
                    "-Xes-long-as-bigint",
                    "-Xir-generate-inline-anonymous-functions",
                )
            )
        }
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

dependencies {
    "webMainApi"("org.jetbrains.kotlin-wrappers:kotlin-browser")
    "webMainApi"(npmConstrained("react-router-dom"))
    "jsMainApi"("org.jetbrains.kotlin-wrappers:kotlin-react")
    "jsMainApi"("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
}
