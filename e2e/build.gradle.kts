import com.zegreatrob.coupling.plugins.util.fetchAwsSsmParameter
import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.jsmints.plugins.wdiotest")
    alias(libs.plugins.apollo)
}

kotlin {
    js {
        useCommonJs()
        nodejs { testTask { enabled = false } }
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

dependencies {
    clientConfiguration(project(":client"))
    appConfiguration(project(":server"))
    jsE2eTestImplementation(platform(project(":libraries:dependency-bom")))
    jsE2eTestImplementation(project(":sdk"))
    jsE2eTestImplementation(project(":libraries:test-logging"))
    jsE2eTestImplementation(project(":libraries:stub-model"))
    jsE2eTestImplementation(kotlin("test-js"))
    jsE2eTestImplementation("com.zegreatrob.jsmints:wdio")
    jsE2eTestImplementation("com.zegreatrob.jsmints:wdio-testing-library")
    jsE2eTestImplementation("com.zegreatrob.testmints:async")
    jsE2eTestImplementation("com.zegreatrob.testmints:minassert")
    jsE2eTestImplementation("com.zegreatrob.testmints:standard")
    jsE2eTestImplementation("io.github.oshai:kotlin-logging")
    jsE2eTestImplementation("io.ktor:ktor-client-content-negotiation")
    jsE2eTestImplementation("io.ktor:ktor-client-core")
    jsE2eTestImplementation("io.ktor:ktor-client-logging")
    jsE2eTestImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsE2eTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsE2eTestImplementation(npmConstrained("fs-extra"))
    jsE2eTestImplementation(npmConstrained("jwt-decode"))
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
        dependsOn("dependencyResources")
    }

    val dependencyResources = register<Copy>("dependencyResources") {
        dependsOn(":sdk:jsProcessResources")
        into(jsE2eTestProcessResources.map { it.destinationDir })
        from("$rootDir/sdk/build/processedResources/js/main")
    }
    e2eRun {
        val testFilter = providers.environmentVariable("FGREP").orElse("")
        inputs.property("testFilter", testFilter)
        environment("FGREP", testFilter.get())
        mustRunAfter(":sdk:jvmTest", ":sdk:jsNodeTest")
        val testPassword = providers.fetchAwsSsmParameter("/local/COUPLING_E2E_TEST_PASSWORD")
        val testLogFile = rootProject.layout.buildDirectory.file("test-output/test.jsonl")
        dependsOn(
            dependencyResources,
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

val browserDiagnosticsTest = tasks.register<NodeExec>("browserDiagnosticsTest") {
    setup(project)
    dependsOn(":kotlinNodeJsSetup")
    taskWorkingDirectory.set(layout.projectDirectory)
    dependsOn(":kotlinStoreYarnLock")
    inputs.property("nodeExecutable", nodeExecPath)
    inputs.file(rootProject.layout.projectDirectory.file("kotlin-js-store/yarn.lock"))
    arguments.set(listOf("--test", "src/test/js/browser-diagnostics.test.mjs"))
    inputs.dir(layout.projectDirectory.dir("diagnostics"))
    inputs.dir(layout.projectDirectory.dir("src/test/js"))
}
tasks.named("check") { dependsOn(browserDiagnosticsTest) }

val browserDiagnosticsBrowserTest = tasks.register<NodeExec>("browserDiagnosticsBrowserTest") {
    setup(project)
    dependsOn(":kotlinNpmInstall")
    taskWorkingDirectory.set(layout.projectDirectory)
    dependsOn(":kotlinStoreYarnLock")
    inputs.property("nodeExecutable", nodeExecPath)
    inputs.file(rootProject.layout.projectDirectory.file("kotlin-js-store/yarn.lock"))
    arguments.set(listOf("--test", "src/test/js/browser-diagnostics.browser.test.mjs"))
    inputs.dir(layout.projectDirectory.dir("diagnostics"))
    inputs.dir(layout.projectDirectory.dir("src/test/js"))
}
tasks.named("check") { dependsOn(browserDiagnosticsBrowserTest) }
tasks.named("e2eRun") { mustRunAfter(browserDiagnosticsBrowserTest) }
tasks.named("copyReportsToCircleCIDirectory") { mustRunAfter(browserDiagnosticsBrowserTest, browserDiagnosticsTest) }
