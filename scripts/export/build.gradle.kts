import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    kotlin("plugin.serialization")
}
kotlin {
    targets {
        jvm()
        js {
            nodejs {
                binaries.executable()
            }
            useCommonJs()
        }
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
    commonMainApi("com.benasher44:uuid")
    commonMainApi("io.github.oshai:kotlin-logging")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "jsMainApi"(project(":libraries:repository:dynamo"))
    "jsMainApi"(project(":libraries:json"))
}

val outputFile: String? by project

tasks {
    named("jsNodeRun", NodeJsExec::class) {
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}
