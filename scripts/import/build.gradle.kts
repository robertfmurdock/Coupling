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
                api(project(":libraries:json"))
                api(project(":libraries:repository:dynamo"))
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
