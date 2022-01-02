import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.plugins.NodeExec
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.node")
    id("com.zegreatrob.coupling.plugins.serialization")
}

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attribute(ProjectLocalConfigurations.ATTRIBUTE, ProjectLocalConfigurations.PUBLIC_VALUE)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

val testLoggingLib: Configuration by configurations.creating

kotlin {
    js {
        nodejs {}
        compilations {
            val endpointTest by compilations.creating
            binaries.executable(endpointTest)
        }
    }
    sourceSets {
        val main by getting
        val test by getting
        val endpointTest by getting {
            dependsOn(main)
            dependsOn(test)
        }
        all { languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi") }
    }
}

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    testLoggingLib(project(mapOf("path" to ":test-logging", "configuration" to "testLoggingLib")))
}

dependencies {
    implementation(project(":model"))
    implementation(project(":repository-core"))
    implementation("com.zegreatrob.testmints:minjson")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation(project(":json"))
    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-serialization:1.6.7")
    implementation("io.ktor:ktor-client-logging:1.6.7")
    implementation("com.soywiz.korlibs.klock:klock:2.4.10")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")

    testImplementation(project(":repository-validation"))
    testImplementation(project(":test-logging"))
    testImplementation(project(":stub-model"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.benasher44:uuid:0.3.1")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.zegreatrob.testmints:async")

    testImplementation(project(":server"))
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
}

tasks {

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileEndpointTestKotlinJs by getting(Kotlin2JsCompile::class) {
        dependsOn("generateExternalsIntegrated")
    }
    val endpointTestEndpointTestProductionExecutableCompileSync by getting {}
    val endpointTestEndpointTestDevelopmentExecutableCompileSync by getting {}

    val compileEndpointTestProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}

    val endpointTest by creating(NodeExec::class) {
        dependsOn(
            "assemble",
            compileKotlinJs,
            compileTestKotlinJs,
            compileEndpointTestKotlinJs,
            compileEndpointTestProductionExecutableKotlinJs,
            endpointTestEndpointTestProductionExecutableCompileSync,
            endpointTestEndpointTestDevelopmentExecutableCompileSync,
            testLoggingLib,
            appConfiguration,
            ":composeUp"
        )
        inputs.file(projectDir.path + "/endpoint-wrapper.js")
        inputs.files(appConfiguration, testLoggingLib)

        outputs.dir("build/test-results/test")

        val processResources = withType(ProcessResources::class.java)

        dependsOn(processResources)

        inputs.files(compileEndpointTestProductionExecutableKotlinJs.outputFileProperty)
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

        arguments = listOf(
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
