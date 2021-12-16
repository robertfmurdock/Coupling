import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
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
                api(kotlin("stdlib", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.4.8")
                api("com.benasher44:uuid:0.3.1")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":json"))
                api(project(":repository-mongo"))
                api(project(":repository-dynamo"))

                api(kotlin("stdlib-js", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))

                implementation(npm("monk", "7.1.1"))
                implementation(npm("mongodb", "3.5.0"))
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