import com.zegreatrob.coupling.build.BuildConstants.kotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
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
                api(project(":model"))
                api(kotlin("stdlib", kotlinVersion))
                api(kotlin("stdlib-common", kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.4.10")
                api("com.benasher44:uuid:0.3.1")
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

val outputFile: String? by project

tasks {
    val jsNodeRun by getting(NodeJsExec::class) {
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}
