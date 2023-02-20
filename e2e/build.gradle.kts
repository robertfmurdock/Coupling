@file:Suppress("UnstableApiUsage")

import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension

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
        .filterIsInstance<DefaultProjectDependency>()
        .map { it.dependencyProject }
        .flatMap { sequenceOf(it) + it.relatedProjects() }
        .plus(this)
        .toSet()
}

kotlin {
    sourceSets {
        getByName("e2eTest") {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":coupling-libraries:test-logging"))
                implementation(kotlin("test-js"))
                implementation("io.github.microutils:kotlin-logging")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.jsmints:wdio")
                implementation("com.zegreatrob.jsmints:wdio-testing-library")

                implementation(npmConstrained("@log4js-node/log4js-api"))
                implementation(npmConstrained("@rpii/wdio-html-reporter"))
                implementation(npmConstrained("@testing-library/webdriverio"))
                implementation(npmConstrained("@wdio/allure-reporter"))
                implementation(npmConstrained("@wdio/cli"))
                implementation(npmConstrained("@wdio/dot-reporter"))
                implementation(npmConstrained("@wdio/jasmine-framework"))
                implementation(npmConstrained("@wdio/local-runner"))
                implementation(npmConstrained("@wdio/junit-reporter"))
                implementation(npmConstrained("allure-commandline"))
                implementation(npmConstrained("chromedriver"))
                implementation(npmConstrained("fs-extra"))
                implementation(npmConstrained("webpack"))
                implementation(npmConstrained("webpack-node-externals"))
                implementation(npmConstrained("wdio-chromedriver-service"))
                implementation(npmConstrained("css-loader"))
                implementation(npmConstrained("url-loader"))
                implementation(npmConstrained("jwt-decode"))
            }
        }
    }
}

dependencies {
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    implementation(kotlin("stdlib-js"))
    implementation("com.benasher44:uuid")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("com.zegreatrob.jsmints:wdio")
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
        into(e2eTestProcessResources.map { it.destinationDir })
        from("$rootDir/client/build/processedResources/js/main")
    }

    val wdioConfig = project.projectDir.resolve("wdio.conf.js")
    val webpackConfig = project.projectDir.resolve("webpack.config.js")
    val webpackedWdioConfigOutput = "config"

    val e2eRun = register("e2eRun", NodeExec::class) {
        setup(project)
        nodeModulesDir = e2eTestProcessResources.get().destinationDir
        moreNodeDirs = listOf(
            "${project.rootProject.buildDir.path}/js/node_modules",
            e2eTestProcessResources.get().destinationDir,
        ).plus(project.relatedResources())
            .joinToString(":")
        outputs.cacheIf { true }
        dependsOn(
            dependencyResources,
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
        inputs.files(compileProductionExecutableKotlinJs.map { it.outputs.files })
        inputs.files(compileE2eTestProductionExecutableKotlinJs.map { it.outputs.files })
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

        arguments = listOf(compileProductionExecutableKotlinJs.get().outputFileProperty.get().absolutePath)
        outputFile = logFile
    }

    named("check") {
        dependsOn(e2eRun)
    }

    named("test") {
        dependsOn(e2eRun)
    }
}

rootProject.extensions.findByType(NodeJsRootExtension::class.java).let {
    it?.nodeVersion = "19.6.0"
}
