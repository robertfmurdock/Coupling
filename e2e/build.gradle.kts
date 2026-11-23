import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.coupling.plugins.util.fetchAwsSsmParameter
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.ir.JsIrBinary
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import com.zegreatrob.jsmints.plugins.WdioTest
import com.zegreatrob.jsmints.plugins.setup as setupWdio

plugins {
    kotlin("plugin.serialization")
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.apollo)
}

kotlin {
    // Share helper directories, not source sets across independent compilation trees.
    sourceSets.named("jsTest") {
        kotlin.srcDir("src/diagnosticsTestSupport/kotlin")
    }
    sourceSets.named("jsE2eTest") {
        kotlin.srcDir("src/browserTestSupport/kotlin")
    }
    js {
        useCommonJs()
        compilations.matching { it.name in setOf("test", "diagnosticsBrowserTest") }.configureEach {
            // The standard TestMints Node runner loads ES modules; application
            // E2E tests retain WebdriverIO's existing CommonJS configuration.
            @Suppress("DEPRECATION")
            compilerOptions.options.moduleKind.set(JsModuleKind.MODULE_ES)
            compileTaskProvider.configure {
                compilerOptions.moduleKind.set(JsModuleKind.MODULE_ES)
            }
            binaries.withType<JsIrBinary>().configureEach {
                linkTask.configure { compilerOptions.moduleKind.set(JsModuleKind.MODULE_ES) }
            }
        }
        val diagnosticsBrowserTest = compilations.create("diagnosticsBrowserTest") {
            associateWith(this@js.compilations.getByName("main"))
            defaultSourceSet.kotlin.srcDirs(
                "src/diagnosticsTestSupport/kotlin",
                "src/browserCaptureSupport/kotlin",
            )
        }
        val browserTestBinary = binaries.executable(diagnosticsBrowserTest)
            .single { it.mode == KotlinJsBinaryMode.DEVELOPMENT }
        nodejs {
            testTask {
                enabled = true
                useMocha { timeout = "15s" }
            }
            testRuns.create("diagnosticsBrowser") {
                setExecutionSourceFrom(diagnosticsBrowserTest)
                executionTask.configure {
                    val chromeBinary = providers.environmentVariable("WDIO_CHROME_BINARY").orElse("")
                    inputs.property("chromeBinary", chromeBinary)
                    environment("WDIO_CHROME_BINARY", chromeBinary.get())
                    inputFileProperty.set(browserTestBinary.mainFileSyncPath)
                    dependsOn(browserTestBinary.linkSyncTask)
                    useMocha { timeout = "60s" }
                }
            }
        }
    }
}

val appConfiguration: Configuration = configurations.create("appConfiguration") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "server")
    }
}

val testLoggingLib: Configuration = configurations.create("testLoggingLib") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "test-logging")
    }
}

val clientConfiguration: Configuration = configurations.create("clientConfiguration") {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "client")
    }
}

apollo {
    service("service") {
        packageName.set("com.zegreatrob.coupling.e2e.gql")
        dependsOn(dependencies.project(":sdk"))
        srcDir("src/jsE2eTest/graphql")
        generateSourcesDuringGradleSync.set(true)
        outputDirConnection {
            connectToKotlinSourceSet("jsE2eTest")
        }
    }
}

val captureCompilation = kotlin.js().compilations.create("visualEvidence") {
    associateWith(kotlin.js().compilations.getByName("main"))
    defaultSourceSet.kotlin.srcDirs(
        "src/browserTestSupport/kotlin",
        "src/browserCaptureSupport/kotlin",
    )
}

dependencies {
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    listOf(
        "jsTestImplementation",
        "jsDiagnosticsBrowserTestImplementation",
        "jsE2eTestImplementation",
        "jsVisualEvidenceImplementation",
    ).forEach { supportImplementation ->
        add(supportImplementation, kotlin("test-js"))
        add(supportImplementation, "com.zegreatrob.testmints:async")
        add(supportImplementation, "com.zegreatrob.testmints:minassert")
        add(supportImplementation, "com.zegreatrob.testmints:standard")
        add(supportImplementation, "org.jetbrains.kotlinx:kotlinx-serialization-json")
    }
    clientConfiguration(project(":client"))
    appConfiguration(project(":server"))
    listOf("jsE2eTestImplementation", "jsVisualEvidenceImplementation").forEach { supportImplementation ->
        add(supportImplementation, platform(project(":libraries:dependency-bom")))
        add(supportImplementation, project(":sdk"))
        add(supportImplementation, "com.zegreatrob.jsmints:wdio")
        add(supportImplementation, "com.zegreatrob.jsmints:wdio-testing-library")
        add(supportImplementation, "io.github.oshai:kotlin-logging")
        add(supportImplementation, "io.ktor:ktor-client-content-negotiation")
        add(supportImplementation, "io.ktor:ktor-client-core")
        add(supportImplementation, "io.ktor:ktor-client-logging")
        add(supportImplementation, "io.ktor:ktor-serialization-kotlinx-json")
        add(supportImplementation, npmConstrained("jwt-decode"))
    }
    jsE2eTestImplementation(project(":libraries:test-logging"))
    jsE2eTestImplementation(project(":libraries:stub-model"))
    jsE2eTestImplementation(npmConstrained("fs-extra"))
}

wdioTest {
    baseUrl.set("https://localhost/")
    htmlReporter.set(true)
    screenshotsOnFailure.set(false)
    useHeadless.set(true)
    chromeBinary.set(System.getenv("WDIO_CHROME_BINARY"))
}

tasks {

    jsE2eTestProcessResources {
        from(layout.projectDirectory.dir("diagnostics"))
    }

    e2eRun {
        val testFilter = providers.environmentVariable("FGREP").orElse("")
        inputs.property("testFilter", testFilter)
        environment("FGREP", testFilter.get())
        mustRunAfter(":sdk:jvmTest", ":sdk:jsNodeTest")
        val testPassword = providers.fetchAwsSsmParameter("/local/COUPLING_E2E_TEST_PASSWORD")
        val testLogFile = rootProject.layout.buildDirectory.file("test-output/test.jsonl")
        dependsOn(
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
        doFirst {
            environment("COUPLING_E2E_TEST_PASSWORD", testPassword.get())
            testLogFile.get().asFile.parentFile.mkdirs()
            environment("COUPLING_TEST_LOG_PATH", testLogFile.get().asFile.absolutePath)
        }
        environment(
            "NODE_TLS_REJECT_UNAUTHORIZED" to 0,
            "COUPLING_TEST_RUN_ID" to (System.getProperty("testRunIdentifier") ?: "unknown-run"),
            "COUPLING_TEST_TASK" to path,
        )
    }

}

tasks.withType<KotlinJsTest>().configureEach {
    val collector = layout.projectDirectory.file("diagnostics/browser-diagnostics.mjs")
    inputs.file(collector)
    environment("COUPLING_DIAGNOSTICS_MODULE", collector.asFile.toURI().toString())
    environment("COUPLING_REPOSITORY_ROOT", rootProject.layout.projectDirectory.asFile.absolutePath)
}

val browserDiagnosticsTest = tasks.register("browserDiagnosticsTest") { dependsOn("jsNodeTest") }
val browserDiagnosticsBrowserTest = tasks.register("browserDiagnosticsBrowserTest") { dependsOn("jsNodeDiagnosticsBrowserTest") }
tasks.named("check") { dependsOn(browserDiagnosticsTest, browserDiagnosticsBrowserTest) }
tasks.named("e2eRun") { mustRunAfter("jsNodeDiagnosticsBrowserTest") }
tasks.named("copyReportsToCircleCIDirectory") { mustRunAfter("jsNodeDiagnosticsBrowserTest", "jsNodeTest") }


val performanceSources = layout.projectDirectory.dir("performance")
val captureBinary = kotlin.js().binaries.executable(captureCompilation)
    .single { it.mode == KotlinJsBinaryMode.DEVELOPMENT }
tasks.register<WdioTest>("captureVisualEvidence") {
    description = "Captures a named local fixture as labeled PNG evidence with a measured manifest"
    group = "verification"
    setupWdio(captureCompilation)
    val runnerProject = kotlin.js().compilations.getByName("e2eTest").npmProject.dir
    val captureProject = captureCompilation.npmProject.dir
    dependsOn(captureBinary.linkSyncTask, "copyWdio", "installRunner", appConfiguration, clientConfiguration, ":composeUp")
    inputs.files(appConfiguration, clientConfiguration)
    inputs.files(captureBinary.linkSyncTask, tasks.named("copyWdio"), tasks.named("copyWdioConfDir"))
    inputs.file(layout.projectDirectory.file("wdio.visual-evidence.conf.mjs"))
    val fixture = providers.gradleProperty("captureFixture").orElse("contribution-overview")
    val label = providers.gradleProperty("captureLabel").orElse("Capture")
    val viewport = providers.gradleProperty("captureViewport").orElse("1280x900")
    inputs.property("fixture", fixture)
    inputs.property("label", label)
    inputs.property("viewport", viewport)
    outputs.upToDateWhen { false }
    val report = layout.buildDirectory.dir("reports/visual-evidence")
    reportDirectory.set(layout.buildDirectory.dir("reports/visual-evidence-runner"))
    testResultsDirectory.set(layout.buildDirectory.dir("visual-evidence-runner/test-results"))
    outputs.dir(report)
    npmWorkingDirectory.set(captureProject)
    specCandidates.set(captureProject.map { listOf(it.file("kotlin/Coupling-e2e-visualEvidence.js")) })
    argumentList.set(runnerProject.map { listOf(it.file("runner/jsmints-wdiorunner.js").asFile.absolutePath) })
    environment("WDIO_CONFIG", layout.projectDirectory.file("wdio.visual-evidence.conf.mjs").asFile.absolutePath)
    environment("COUPLING_CAPTURE_BASE_CONFIG", runnerProject.get().file("wdio.conf.mjs").asFile.toURI().toString())
    environment("BASEURL", "https://localhost/")
    environment("REPORT_DIR", reportDirectory.get().asFile.absolutePath)
    environment("TEST_RESULTS_DIR", testResultsDirectory.get().asFile.absolutePath)
    environment("LOGS_DIR", layout.buildDirectory.dir("reports/logs/visual-evidence").get().asFile.absolutePath)
    environment("NODE_TLS_REJECT_UNAUTHORIZED", "0")
    environment("COUPLING_REPOSITORY_ROOT", rootProject.layout.projectDirectory.asFile.absolutePath)
    environment("COUPLING_CAPTURE_DIRECTORY", report.get().asFile.absolutePath)
    environment("COUPLING_CAPTURE_POLICY_MODULE", captureProject.get().file("kotlin/Coupling-e2e.js").asFile.absolutePath)
    environment("COUPLING_CAPTURE_DIAGNOSTICS_MODULE", layout.projectDirectory.file("diagnostics/browser-diagnostics.mjs").asFile.toURI().toString())
    environment("COUPLING_CAPTURE_FIXTURE", fixture.get())
    environment("COUPLING_CAPTURE_LABEL", label.get())
    environment("COUPLING_CAPTURE_VIEWPORT", viewport.get())
    val testPassword = providers.fetchAwsSsmParameter("/local/COUPLING_E2E_TEST_PASSWORD")
    doFirst { environment("COUPLING_E2E_TEST_PASSWORD", testPassword.get()) }
}
val visualEvidenceTest = tasks.register("visualEvidenceTest") {
    description = "Tests the visual evidence manifest and failure contracts without a browser"
    group = "verification"
    dependsOn("jsNodeTest")
}
tasks.named("check") { dependsOn(visualEvidenceTest) }
tasks.withType<KotlinJsTest>().configureEach { inputs.dir(performanceSources) }
tasks.register<NodeExec>("measureWelcomePerformance") {
    description = "Records six bounded Welcome lab samples; requires browser execution outside the sandbox"
    group = "verification"
    setup(project)
    taskWorkingDirectory.set(rootProject.layout.projectDirectory)
    dependsOn(appConfiguration, clientConfiguration, ":composeUp", ":kotlinNpmInstall")
    val report = layout.buildDirectory.file("reports/web-performance/welcome.json")
    val chromeBinary = providers.environmentVariable("WDIO_CHROME_BINARY").orElse("")
    inputs.dir(performanceSources)
    inputs.property("chromeBinary", chromeBinary)
    outputs.file(report)
    outputs.upToDateWhen { false }
    arguments.set(listOf(
        performanceSources.file("measure-welcome.mjs").asFile.absolutePath,
        rootProject.layout.projectDirectory.asFile.absolutePath,
        report.get().asFile.absolutePath,
        chromeBinary.get(),
    ))
}
