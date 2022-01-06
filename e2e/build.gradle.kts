
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.FileOutputStream

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attribute(ProjectLocalConfigurations.ATTRIBUTE, ProjectLocalConfigurations.PUBLIC_VALUE)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

val testLoggingLib: Configuration by configurations.creating { }

val clientConfiguration: Configuration by configurations.creating

kotlin {
    sourceSets {
        val e2eTest by getting {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":test-logging"))
                implementation(kotlin("test-js"))
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:wdio")
                implementation(appConfiguration)
                jstools.packageJson.devDependencies()?.forEach {
                    implementation(npm(it.first, it.second.asText()))
                }
            }
        }
    }
}

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    testLoggingLib(project(mapOf("path" to ":test-logging", "configuration" to "testLoggingLib")))

    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("com.benasher44:uuid:0.3.1")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.zegreatrob.testmints:wdio")
    jstools.packageJson.devDependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }
}

tasks {
    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileE2eTestProductionExecutableKotlinJs by getting {}
    val productionExecutableCompileSync by getting {}

    val e2eTestProcessResources by getting(ProcessResources::class)

    val dependencyResources by creating(Copy::class) {
        dependsOn(":client:processResources")
        duplicatesStrategy = DuplicatesStrategy.WARN
        into(e2eTestProcessResources.destinationDir)
        from("$rootDir/client/build/processedResources/js/main")
    }

    e2eTestProcessResources.dependsOn(dependencyResources)

    val wdioConfig = project.projectDir.resolve("wdio.conf.js")
    val webpackConfig = project.projectDir.resolve("webpack.config.js")
    val webpackedWdioConfigOutput = "config"

    val nodeRun by getting(NodeJsExec::class) {
        dependsOn(
            compileProductionExecutableKotlinJs,
            productionExecutableCompileSync,
            compileE2eTestProductionExecutableKotlinJs,
            appConfiguration,
            clientConfiguration,
            testLoggingLib,
            ":composeUp"
        )
        inputs.files(
            appConfiguration,
            clientConfiguration,
            testLoggingLib
        )
        inputs.files(compileProductionExecutableKotlinJs.outputs.files)
        inputs.files(compileE2eTestProductionExecutableKotlinJs.outputs.files)
        inputs.files(wdioConfig)

        val reportDir = "${project.buildDir.absolutePath}/reports/e2e-serverless/"
        outputs.dir(reportDir)
        val logsDir = "${project.buildDir.absolutePath}/reports/logs/e2e-serverless/"

        environment("BASEURL" to "https://localhost/local/")
        environment(
            mapOf(
                "TEST_LOGIN_ENABLED" to "true",
                "CLIENT_BASENAME" to "local",
                "SERVER_DIR" to project(":server").projectDir.absolutePath,
                "NODE_PATH" to listOf(
                    "${project.rootProject.buildDir.path}/js/node_modules",
                    e2eTestProcessResources.destinationDir
                ).joinToString(":"),
                "BUILD_DIR" to project.buildDir.absolutePath,
                "WEBPACK_CONFIG" to webpackConfig,
                "WEBPACKED_WDIO_CONFIG_OUTPUT" to webpackedWdioConfigOutput,
                "REPORT_DIR" to reportDir,
                "LOGS_DIR" to logsDir,
                "NODE_TLS_REJECT_UNAUTHORIZED" to 0,
                "STRICT_SSL" to "false",
            )
        )
        val logFile = file("${logsDir}/run.log")
        logFile.parentFile.mkdirs()
        standardOutput = FileOutputStream(logFile, true)
    }

    val check by getting {
        dependsOn(nodeRun)
    }
}
