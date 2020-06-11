import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js { nodejs() }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(kotlin("stdlib", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", com.zegreatrob.coupling.build.BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:1.10.6")
                api("com.benasher44:uuid:0.1.0")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":json"))
                api(project(":repository:mongo"))
                api(project(":repository:dynamo"))
                api("io.github.microutils:kotlin-logging-js:1.7.10")

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