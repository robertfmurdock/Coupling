
import com.moowork.gradle.node.task.NodeTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.3.72"
    id("com.github.node-gradle.node")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

val packageJson = loadPackageJson()

kotlin {

    js {
        nodejs {}
        compilations {
            val endpointTest by compilations.creating
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":repository"))
                api("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")
                api("com.soywiz.korlibs.klock:klock:1.10.6")
                implementation("io.github.microutils:kotlin-logging-common:1.8.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.20.0-1.3.70-eap-274-2")
            }
        }
        val commonTest by getting {
            dependencies {
                api(project(":repository:validation"))
                implementation(project(":json"))
                implementation(project(":test-logging"))
                implementation(project(":stub-model"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.benasher44:uuid:0.1.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")

                packageJson.dependencies().forEach {
                    implementation(npm(it.first, it.second.asText()))
                }
            }
        }

        val jsEndpointTest by getting {
            dependsOn(jsMain)
            dependsOn(commonTest)

            dependencies {
                implementation(project(":server"))
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.zegreatrob.testmints:async:+")

                packageJson.devDependencies().forEach {
                    implementation(npm(it.first, it.second.asText()))
                }

            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.zegreatrob.testmints:async:+")

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
            "assemble",
            compileKotlinJs,
            compileTestKotlinJs,
            compileEndpointTestKotlinJs,
            ":server:build"
        )
        val script = projectDir.path + "/endpoint-wrapper.js"
        inputs.file(script)
        inputs.files(findByPath(":server:serverCompile")!!.outputs)

        setScript(File(script))
        outputs.dir("build/test-results/jsTest")

        val processResources = withType(ProcessResources::class.java)

        dependsOn(processResources)

        val relevantPaths = listOf(
            "../build/js/node_modules"
        ) + processResources.map { it.destinationDir.path }

        inputs.files(compileEndpointTestKotlinJs.outputFile)

        relevantPaths.forEach { if (File(it).isDirectory) inputs.dir(it) }

        setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

        setArgs(listOf("${compileEndpointTestKotlinJs.outputFile}"))
    }

    val check by getting {
        dependsOn(endpointTest)
    }

}

