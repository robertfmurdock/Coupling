import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeExecPath
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kotlinx-serialization") version "1.5.21"
}

val packageJson = loadPackageJson()

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(
            org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute.jsCompilerAttribute,
            org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute.ir
        )
        attribute(
            org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations.ATTRIBUTE,
            org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations.PUBLIC_VALUE
        )
        attribute(
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.js
        )
    }
}

val testLoggingLib: Configuration by configurations.creating {
}

kotlin {
    js {
        useCommonJs()
        nodejs {}
        binaries.executable()
        compilations {
            val endpointTest by compilations.creating
            binaries.executable(endpointTest)
        }
    }
    sourceSets {
        all {
            languageSettings {
                useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation(project(":repository-core"))
                implementation("com.zegreatrob.testmints:minjson:4.1.2")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("io.github.microutils:kotlin-logging:2.0.10")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.213-kotlin-1.5.10")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":repository-validation"))
                implementation(project(":test-logging"))
                implementation(project(":stub-model"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard:4.1.11")
                implementation("com.zegreatrob.testmints:minassert:4.1.11")
                implementation("com.benasher44:uuid:0.2.4")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")

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
                implementation("com.zegreatrob.testmints:standard:4.1.11")
                implementation("com.zegreatrob.testmints:minassert:4.1.11")
                implementation("com.zegreatrob.testmints:async:4.1.11")

                packageJson.devDependencies().forEach {
                    implementation(npm(it.first, it.second.asText()))
                }

            }
        }

        val jsTest by getting {
            dependencies {
                implementation("com.zegreatrob.testmints:standard:4.1.11")
                implementation("com.zegreatrob.testmints:minassert:4.1.11")
                implementation("com.zegreatrob.testmints:async:4.1.11")
            }
        }
    }
}

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    testLoggingLib(project(mapOf("path" to ":test-logging", "configuration" to "testLoggingLib")))
}

tasks {

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileEndpointTestKotlinJs by getting(Kotlin2JsCompile::class) {
        dependsOn("jsGenerateExternalsIntegrated")
    }
    val jsEndpointTestEndpointTestProductionExecutableCompileSync by getting {}
    val jsEndpointTestEndpointTestDevelopmentExecutableCompileSync by getting {}

    val compileEndpointTestProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}

    val endpointTest by creating(Exec::class) {
        dependsOn(
            "assemble",
            compileKotlinJs,
            compileTestKotlinJs,
            compileEndpointTestKotlinJs,
            compileEndpointTestProductionExecutableKotlinJs,
            jsEndpointTestEndpointTestProductionExecutableCompileSync,
            jsEndpointTestEndpointTestDevelopmentExecutableCompileSync,
            testLoggingLib,
            appConfiguration
        )
        inputs.file(projectDir.path + "/endpoint-wrapper.js")
        inputs.files(appConfiguration, testLoggingLib)

        outputs.dir("build/test-results/jsTest")

        val processResources = withType(ProcessResources::class.java)

        dependsOn(processResources)

        inputs.files(compileEndpointTestProductionExecutableKotlinJs.outputFile)

        val relevantPaths = listOf(
            "$nodeModulesDir",
            "$nodeModulesDir/../packages/Coupling-sdk-endpointTest/node_modules"
        ) + processResources.map { it.destinationDir.path }
        relevantPaths.forEach { if (File(it).isDirectory) inputs.dir(it) }
        val serverlessConfigFile = "${project(":server").projectDir.absolutePath}/serverless.yml"
        environment(
            "NODE_PATH" to relevantPaths.joinToString(":"),
            "TEST_LOGIN_ENABLED" to "true",
            "PORT" to "4001",
            "WEBSOCKET_HOST" to "localhost:4002",
            "LAMBDA_ENDPOINT" to "http://localhost:4003",
            "APP_PATH" to "${rootProject.buildDir.absolutePath}/js/node_modules/.bin/serverless offline --config $serverlessConfigFile --httpPort 4001 --websocketPort 4002 --lambdaPort 4003",
            "BASEURL" to "http://localhost:4001/local/",
            "SERVER_DIR" to project(":server").projectDir.absolutePath,
            "CLIENT_BASENAME" to "local",
            "BASENAME" to "local",
        )
        commandLine = listOf(
            nodeExecPath,
            "--unhandled-rejections=strict",
            project.relativePath("endpoint-wrapper"),
            "${compileEndpointTestProductionExecutableKotlinJs.outputFile}"
        )

        val logsDir = "${project.buildDir.absolutePath}/reports/tests/"
        outputs.dir(logsDir)
        val logFile = file("${logsDir}/run.log")
        logFile.parentFile.mkdirs()
        standardOutput = logFile.outputStream()
    }

    val check by getting {
        dependsOn(endpointTest)
    }

}
