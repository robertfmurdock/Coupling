import com.moowork.gradle.node.task.NodeTask
import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.3.61"
    id("smol-js")
}

kotlin {

    js {
        nodejs {
            testTask {
                enabled = false
            }
        }

        compilations {
            val endpointTest by compilations.creating
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2-1.3.60")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.2-1.3.60")
                implementation("com.soywiz.korlibs.klock:klock:1.8.1")
                implementation("io.github.microutils:kotlin-logging-common:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0-1.3.60-eap-76")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation(project(":test-logging"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":json"))
                implementation(npm("axios", "^0.19.0"))
                implementation(npm("axios-cookiejar-support", "^0.5.0"))
                implementation(npm("fs-extra", "^8.1.0"))
                implementation(npm("monk", "^7.1.1"))
                implementation(npm("tough-cookie", "^3.0.1"))
                implementation(npm("ws", "^7.2.0"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.2-1.3.60")
                implementation("io.github.microutils:kotlin-logging-js:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0-1.3.60-eap-76")
            }
        }

        val jsEndpointTest by getting {
            dependsOn(jsMain)
            dependencies {
                implementation(project(":server"))
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.zegreatrob.testmints:async-js:+")
                implementation("com.benasher44:uuid-js:0.0.5")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("com.benasher44:uuid-js:0.0.5")
            }
        }
    }
}

tasks {

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }

    val compileEndpointTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }

    val endpointTest by creating(NodeTask::class) {
        dependsOn(
            "yarn",
            "assemble",
            compileKotlinJs,
            compileTestKotlinJs,
            compileEndpointTestKotlinJs,
            ":server:build"
        )
        val script = projectDir.path + "/endpoint-wrapper.js"
        inputs.file(script)
        inputs.file(file("package.json"))

        setScript(File(script))
        outputs.dir("build/test-results/jsTest")
    }
    val check by getting {
        dependsOn(endpointTest)
    }

    afterEvaluate {
        val processResources = tasks.filterIsInstance(ProcessResources::class.java)

        with(endpointTest) {
            dependsOn(processResources)

            val relevantPaths = listOf(
                "node_modules",
                "../build/js/node_modules"
            ) + processResources.map { it.destinationDir.path }

            inputs.files(compileEndpointTestKotlinJs.outputFile)

            relevantPaths.forEach { if (File(it).isDirectory) inputs.dir(it) }

            setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

            setArgs(listOf("${compileEndpointTestKotlinJs.outputFile}"))
        }
    }

}

