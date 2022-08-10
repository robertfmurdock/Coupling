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
        val commonMain by getting {
            dependencies {
                api(project(":coupling-libraries:model"))
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.soywiz.korlibs.klock:klock:3.0.0")
                api("com.benasher44:uuid:0.5.0")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":coupling-libraries:json"))
                api(project(":dynamo"))
                api(kotlin("stdlib-js"))
            }
        }
    }
}

val inputFile: String? by project

tasks {
    named("jsNodeRun", NodeJsExec::class) {
        inputFile?.let {
            standardInput = file(it).inputStream()
        }
    }
}
