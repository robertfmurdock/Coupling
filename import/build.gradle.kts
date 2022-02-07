import com.zegreatrob.coupling.build.BuildConstants.kotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
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
        val commonMain by getting {
            dependencies {
                api("com.zegreatrob.coupling.libraries:model")
                api(kotlin("stdlib", kotlinVersion))
                api(kotlin("stdlib-common", kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.4.13")
                api("com.benasher44:uuid:0.4.0")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":json"))
                api(project(":repository-dynamo"))
                api(kotlin("stdlib-js", kotlinVersion))
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