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
                api(project(":coupling-libraries:json"))
                api(project(":coupling-libraries:dynamo"))
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
