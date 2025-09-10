import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}
kotlin {
    jvm()
    js {
        nodejs {
            binaries.executable()
        }
        useEsModules()
        compilerOptions { target = "es2015" }
    }
    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }
    }
}

dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainApi("io.github.oshai:kotlin-logging")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "jsMainApi"(project(":libraries:repository:dynamo"))
    "jsMainApi"(project(":libraries:json"))
}

val outputFile: String? by project

tasks {
    named("jsNodeProductionRun", NodeJsExec::class) {
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}
