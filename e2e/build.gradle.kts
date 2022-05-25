import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
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

configurations {
    "e2eTestImplementation" {
        extendsFrom(appConfiguration)
    }
}

fun Project.relatedResources() = relatedProjects()
    .asSequence()
    .map { it.projectDir }
    .flatMap {
        listOf(
            "src/commonMain/resources",
            "src/clientCommonMain/resources",
            "src/jsMain/resources",

            "src/main/resources"
        ).asSequence().map(it::resolve)
    }
    .filter { it.exists() }
    .filter { it.isDirectory }
    .toList()

fun Project.relatedProjects(): Set<Project> {
    val configuration = configurations.findByName("e2eTestImplementation")
        ?: return emptySet()

    return configuration
        .allDependencies
        .asSequence()
        .filterIsInstance<org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency>()
        .map { it.dependencyProject }
        .flatMap { sequenceOf(it) + it.relatedProjects() }
        .plus(this)
        .toSet()
}

kotlin {
    sourceSets {
        val e2eTest by getting {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation(kotlin("test-js"))
                implementation("io.github.microutils:kotlin-logging:2.1.23")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.jsmints:wdio")

                jstools.packageJson.devDependencies()?.forEach {
                    implementation(npm(it.first, it.second.asText()))
                }
            }
        }
    }
}

dependencies {
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    implementation(kotlin("stdlib-js"))
    implementation("com.benasher44:uuid:0.4.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.zegreatrob.jsmints:wdio")

    jstools.packageJson.devDependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }
}

tasks {
    val compileE2eTestProductionExecutableKotlinJs = named("compileE2eTestProductionExecutableKotlinJs")
    val productionExecutableCompileSync = named("productionExecutableCompileSync")

    val e2eTestProcessResources = named<ProcessResources>("e2eTestProcessResources") {
        dependsOn("dependencyResources")
    }

    val dependencyResources by registering(Copy::class) {
        dependsOn(":client:processResources")
        duplicatesStrategy = DuplicatesStrategy.WARN
        into(e2eTestProcessResources.get().destinationDir)
        from("$rootDir/client/build/processedResources/js/main")
    }

    val wdioConfig = project.projectDir.resolve("wdio.conf.js")
    val webpackConfig = project.projectDir.resolve("webpack.config.js")
    val webpackedWdioConfigOutput = "config"

    val nodeRun = named("nodeRun", NodeJsExec::class) {
        outputs.cacheIf { true }
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
        inputs.files(compileProductionExecutableKotlinJs.get().outputs.files)
        inputs.files(compileE2eTestProductionExecutableKotlinJs.get().outputs.files)
        inputs.files(wdioConfig)

        val reportDir = "${project.buildDir.absolutePath}/reports/e2e-serverless/"
        outputs.dir(reportDir)
        val testResultsDir = "${project.buildDir.absolutePath}/test-results/"
        outputs.dir(testResultsDir)
        val logsDir = "${project.buildDir.absolutePath}/reports/logs"
        val logFile = file("$logsDir/run.log")
        logFile.parentFile.mkdirs()

        environment("BASEURL" to "https://localhost/local/")
        environment(
            mapOf(
                "CLIENT_BASENAME" to "local",
                "SERVER_DIR" to project(":server").projectDir.absolutePath,
                "NODE_PATH" to listOf(
                    "${project.rootProject.projectDir.path}/coupling-libraries/build/js/node_modules",
                    "${project.rootProject.buildDir.path}/js/node_modules",
                    e2eTestProcessResources.get().destinationDir,
                ).plus(project.relatedResources())
                    .joinToString(":"),
                "BUILD_DIR" to project.buildDir.absolutePath,
                "WEBPACK_CONFIG" to webpackConfig,
                "WEBPACKED_WDIO_CONFIG_OUTPUT" to webpackedWdioConfigOutput,
                "REPORT_DIR" to reportDir,
                "TEST_RESULTS_DIR" to testResultsDir,
                "LOGS_DIR" to logsDir,
                "NODE_TLS_REJECT_UNAUTHORIZED" to 0,
                "STRICT_SSL" to "false",
            )
        )

        standardOutput = FileOutputStream(logFile, true)
    }

    named("check") {
        dependsOn(nodeRun)
    }

    named("test") {
        dependsOn(nodeRun)
    }
}
