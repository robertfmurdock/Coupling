import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import com.zegreatrob.tools.TaggerPlugin
import com.zegreatrob.tools.tagger.ReleaseVersion
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.testmints.action-mint")
    kotlin("plugin.serialization")
    alias(libs.plugins.com.apollographql.apollo)
    alias(libs.plugins.io.github.turansky.kfc.application)
    alias(libs.plugins.io.github.turansky.seskar)
}

kotlin {
    js {
        useEsModules()
        compilerOptions {
            target = "es2015"
            moduleKind = JsModuleKind.MODULE_ES
        }
    }
    sourceSets {
        sourceSets { all { languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop") } }
        jsMain {
            resources.srcDir("src/jsMain/javascript")
            kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
        }
        jsTest {
            kotlin.srcDir("build/generated/ksp/js/jsTest/kotlin")
        }
    }
}

apollo {
    service("service") {
        packageName.set("com.zegreatrob.coupling.client.gql")
        dependsOn(project(":sdk"))
        generateSourcesDuringGradleSync.set(true)
    }
}

val jsRuntimeClasspath: Configuration by configurations.getting
val clientConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "client")
    }
}
val cdnLookupConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "cdnLookupConfiguration")
    }
}

dependencies {
    cdnLookupConfiguration(project(":scripts:cdn-lookup"))
    jsMainImplementation(project("components"))
    jsMainImplementation(project(":sdk"))
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:action"))
    jsMainImplementation(project(":libraries:logging"))
    jsMainImplementation(project(":libraries:repository:core"))
    jsMainImplementation(project(":libraries:repository:memory"))
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("com.zegreatrob.jsmints:react-data-loader")
    jsMainImplementation("com.zegreatrob.testmints:action")
    jsMainImplementation("com.zegreatrob.testmints:action-async")
    jsMainImplementation("io.ktor:ktor-client-content-negotiation")
    jsMainImplementation("io.ktor:ktor-client-core")
    jsMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion-styled")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-datetime")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    jsMainImplementation(npmConstrained("@auth0/auth0-react"))
    jsMainImplementation(npmConstrained("date-fns"))
    jsMainImplementation(npmConstrained("drag-drop-webkit-mobile"))
    jsMainImplementation(npmConstrained("favicons"))
    jsMainImplementation(npmConstrained("fitty"))
    jsMainImplementation(npmConstrained("react-dnd"))
    jsMainImplementation(npmConstrained("react-dnd-html5-backend"))
    jsMainImplementation(npmConstrained("react-use-websocket"))
    jsMainImplementation(npmConstrained("reactjs-popup"))
    jsTestImplementation(npmConstrained("sonda"))
    jsMainImplementation(npmConstrained("vite-plugin-html"))

    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:minspy")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
}

rootProject.apply<TaggerPlugin>()

rootProject.yarn.ignoreScripts = false

tasks {
    val npmProjectDir = kotlin.js().compilations.named("main").map { it.npmProject.dir.get() }
    val cdnBuildOutput = npmProjectDir.map { it.file("cdn.json") }
    val settingsFile = File(project.projectDir, "cdn.settings.json")
    val copyCdnSettings by registering(Copy::class) {
        dependsOn(":kotlinNpmInstall")
        from(settingsFile)
        into(npmProjectDir)
    }
    val lookupCdnUrls by registering(NodeExec::class) {
        setup(project)
        dependsOn(cdnLookupConfiguration, "jsPublicPackageJson", ":kotlinNpmInstall")
        inputs.files(cdnLookupConfiguration)
        inputs.files(jsRuntimeClasspath)
        inputs.file(settingsFile)
        val settings = ObjectMapper().readTree(settingsFile)
        val cdnLookupFile = cdnLookupConfiguration.resolve().first()
        arguments = listOf("--no-warnings", cdnLookupFile.absolutePath) + settings.fieldNames().asSequence().toList()
        outputFile = file(cdnBuildOutput)
        outputs.cacheIf { true }
    }
    val projectResultPath = rootProject.layout.buildDirectory
        .file("test-output/${project.path}/results".replace(":", "/"))
    val copyCdnJsonToResultDirectory by registering(Copy::class) {
        mustRunAfter(check)
        from(cdnBuildOutput)
        into(projectResultPath)
    }
    named("collectResults") {
        dependsOn(copyCdnJsonToResultDirectory)
    }

    named("compileTestDevelopmentExecutableKotlinJs") {
        dependsOn(lookupCdnUrls)
    }
    compileProductionExecutableKotlinJs {}

    jsBrowserProductionVite {
        dependsOn(lookupCdnUrls, copyCdnSettings, jsProcessResources)
        mustRunAfter("components:jsNodeTest")
        inputs.file(cdnBuildOutput)
        inputs.file(project.layout.projectDirectory.file("vite.config.mjs"))
        inputs.dir(jsProcessResources.map { it.destinationDir })
        inputs.file(File(project.projectDir, "cdn.settings.json"))
        outputs.file(outputDirectory.file("client-vendor.js"))
        outputs.file(outputDirectory.file("client-kotlin.js"))
        outputs.file(outputDirectory.file("client-coupling-core.js"))
        outputs.file(outputDirectory.file("client-kotlinx.js"))
        outputs.file(outputDirectory.file("client-ktor.js"))
        outputs.cacheIf { true }
    }
    named("jsBrowserDevelopmentRun") {
        dependsOn(lookupCdnUrls, jsProcessResources)
    }

    val uploadToS3 by registering(Exec::class) {
        dependsOn(jsBrowserProductionVite)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = jsBrowserProductionVite.get().outputDirectory.get().asFile.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${rootProject.version}".split(" ")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release")
        .configure {
            finalizedBy(uploadToS3)
        }

    val additionalResources by registering(Copy::class) {
        outputs.cacheIf { true }
        dependsOn(":sdk:jsProcessResources")
        from(provider { (findByPath(":sdk:jsProcessResources") as ProcessResources).destinationDir })
        into(project.layout.buildDirectory.file("additionalResources"))
    }
    jsProcessResources {
        dependsOn(additionalResources)
    }

    named("jsBrowserTest") {
        outputs.cacheIf { true }
    }
}

tasks {
    withType<FormatTask> {
        dependsOn("kspKotlinJs")
    }
    withType<LintTask> {
        dependsOn("kspKotlinJs")
    }
}

artifacts {
    add(clientConfiguration.name, tasks.compileProductionExecutableKotlinJs.map { it.destinationDirectory }) {
        builtBy(tasks.compileProductionExecutableKotlinJs)
    }
    add(clientConfiguration.name, tasks.jsBrowserProductionVite.map { it.outputDirectory }) {
        builtBy(tasks.jsBrowserProductionVite, tasks.jsBrowserProductionVitePrepare)
    }
}
