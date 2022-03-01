import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
group = "com.zegreatrob.coupling.libraries"
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
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.soywiz.korlibs.klock:klock:2.5.3")
                api("com.benasher44:uuid:0.4.0")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":json"))
                api(project(":repository-dynamo"))
                api(kotlin("stdlib-js"))
            }
        }
    }
}

val inputFile: String? by project

tasks {
    val jsNodeRun by getting(NodeJsExec::class) {
        inputFile?.let {
            standardInput = file(it).inputStream()
        }
    }
}