import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    jvm()
    js {
        nodejs {
            binaries.executable()
        }
        useEsModules()
        compilerOptions { target = "es2015" }
    }
}

dependencies {
    commonMainApi(project(":libraries:model"))
    commonMainApi("io.github.oshai:kotlin-logging")
    commonMainApi("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "jsMainApi"(project(":libraries:json"))
    "jsMainApi"(project(":libraries:repository:dynamo"))
}

val inputFile: String? by project

tasks {
    named("jsNodeProductionRun", NodeJsExec::class) {
        inputFile?.let {
            standardInput = file(it).inputStream()
        }
    }
}
