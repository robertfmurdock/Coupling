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
                api(project(":coupling-libraries:model"))
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.soywiz.korlibs.klock:klock")
                api("com.benasher44:uuid")
            }
        }
        getByName("jsMain") {
            dependencies {
                api(project(":coupling-libraries:dynamo"))
                api(project(":coupling-libraries:json"))

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
