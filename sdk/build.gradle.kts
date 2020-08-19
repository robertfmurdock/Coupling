import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.BuildConstants.testmintsVersion
import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeExecPath
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.4.0"
}

val packageJson = loadPackageJson()

kotlin {

    js {
        nodejs {}
        useCommonJs()
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
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                api("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("io.github.microutils:kotlin-logging-common:1.8.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
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
                implementation("com.zegreatrob.testmints:standard:$testmintsVersion")
                implementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
                implementation("com.benasher44:uuid:0.2.0")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")

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
                implementation("com.zegreatrob.testmints:standard:$testmintsVersion")
                implementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
                implementation("com.zegreatrob.testmints:async:$testmintsVersion")

                packageJson.devDependencies().forEach {
                    implementation(npm(it.first, it.second.asText()))
                }

            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("com.zegreatrob.testmints:standard:$testmintsVersion")
                implementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
                implementation("com.zegreatrob.testmints:async:$testmintsVersion")
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

    val endpointTest by creating(Exec::class) {
        dependsOn(
            "assemble",
            compileKotlinJs,
            compileTestKotlinJs,
            compileEndpointTestKotlinJs,
            ":server:build"
        )
        inputs.file(projectDir.path + "/endpoint-wrapper.js")
        inputs.files(findByPath(":server:serverCompile")!!.outputs)

        outputs.dir("build/test-results/jsTest")

        val processResources = withType(ProcessResources::class.java)

        dependsOn(processResources)

        inputs.files(compileEndpointTestKotlinJs.outputFile)

        val relevantPaths = listOf("$nodeModulesDir") + processResources.map { it.destinationDir.path }
        relevantPaths.forEach { if (File(it).isDirectory) inputs.dir(it) }

        environment(
            "NODE_PATH" to relevantPaths.joinToString(":"),
            "PORT" to "4001"
        )
        commandLine = listOf(
            nodeExecPath,
            project.relativePath("endpoint-wrapper"),
            "${compileEndpointTestKotlinJs.outputFile}"
        )
    }

    val check by getting {
        dependsOn(endpointTest)
    }

}

