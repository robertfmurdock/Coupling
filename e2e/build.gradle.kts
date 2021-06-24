import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
        useCommonJs()
        binaries.executable()
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

val packageJson = loadPackageJson()

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
                implementation("io.github.microutils:kotlin-logging:2.0.6")
                implementation("com.zegreatrob.testmints:standard:4.1.2")
                implementation("com.zegreatrob.testmints:minassert:4.1.2")
                implementation("com.zegreatrob.testmints:async:4.1.2")
                implementation("com.zegreatrob.testmints:wdio:4.1.2")
                implementation(appConfiguration)
                packageJson.devDependencies().forEach {
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
    implementation("com.benasher44:uuid:0.3.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.213-kotlin-1.5.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("com.zegreatrob.testmints:wdio:4.1.2")
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    packageJson.devDependencies().forEach {
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
            testLoggingLib
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
        val logsDir = "${project.buildDir.absolutePath}/logs/e2e-serverless/"
        outputs.dir(logsDir)

        val serverlessConfigFile = "${project(":server").projectDir.absolutePath}/serverless.yml"
        environment("BASEURL" to "http://localhost:3099/local/")
        environment("CLIENT_PATH", file("${rootProject.rootDir.absolutePath}/client/build/distributions"))
        environment(
            mapOf(
                "TEST_LOGIN_ENABLED" to "true",
                "CLIENT_BASENAME" to "local",
                "SERVER_DIR" to project(":server").projectDir.absolutePath,
                "APP_PATH" to "${rootProject.buildDir.absolutePath}/js/node_modules/.bin/serverless offline --config $serverlessConfigFile --httpPort 3099",
                "NODE_PATH" to listOf(
                    "${project.rootProject.buildDir.path}/js/node_modules",
                    e2eTestProcessResources.destinationDir
                ).joinToString(":"),
                "BUILD_DIR" to project.buildDir.absolutePath,
                "WEBPACK_CONFIG" to webpackConfig,
                "WEBPACKED_WDIO_CONFIG_OUTPUT" to webpackedWdioConfigOutput,
                "REPORT_DIR" to reportDir,
                "LOGS_DIR" to logsDir,
            )
        )
        val logFile = file("${logsDir}/run.log")
        logFile.parentFile.mkdirs()
        standardOutput = logFile.outputStream()
    }

    val check by getting {
        dependsOn(nodeRun)
    }
}
