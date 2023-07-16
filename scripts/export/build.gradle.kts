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
        getByName("commonMain") {
            dependencies {
                api(project(":libraries:model"))
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.benasher44:uuid")
                api("io.github.microutils:kotlin-logging")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json")
            }
        }
        getByName("jsMain") {
            dependencies {
                api(project(":libraries:repository:dynamo"))
                api(project(":libraries:json"))

                api(kotlin("stdlib-js"))
            }
        }
    }
}

val outputFile: String? by project

tasks {
    named("jsNodeRun", NodeJsExec::class) {
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}
