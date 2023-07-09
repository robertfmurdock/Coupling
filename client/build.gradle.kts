
import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import com.zegreatrob.tools.tagger.TaggerExtension
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

plugins {
    id("com.zegreatrob.jsmints.plugins.minreact")
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}

kotlin {
    js {
        browser {
            webpackTask(Action {
                dependsOn("additionalResources")
                inputs.files("${project.projectDir}/src/main/resources")
                val profile: String? by project
                if (!profile.isNullOrBlank()) {
                    this.args.add("--profile")
                    this.args.add("--json=$buildDir/reports/stats.json")
                }
            })
        }
    }
    sourceSets {
        getByName("jsMain") {
            resources.srcDir("src/jsMain/javascript")
        }
    }
}

kotlin {
    sourceSets.jsMain {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
    }
    sourceSets.jsTest {
        kotlin.srcDir("build/generated/ksp/js/jsTest/kotlin")
    }
}

val jsRuntimeClasspath: Configuration by configurations.getting
val clientConfiguration: Configuration by configurations.creating
val cdnLookupConfiguration: Configuration by configurations.creating

dependencies {
    cdnLookupConfiguration(
        project(mapOf("path" to ":scripts:cdn-lookup", "configuration" to "cdnLookupConfiguration"))
    )
    jsMainImplementation(kotlin("stdlib-js"))
    jsMainImplementation(project("components"))
    jsMainImplementation(project(":sdk"))
    jsMainImplementation(project(":libraries:model"))
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:action"))
    jsMainImplementation(project(":libraries:logging"))
    jsMainImplementation(project(":libraries:repository:core"))
    jsMainImplementation(project(":libraries:repository:memory"))
    jsMainImplementation("com.benasher44:uuid")
    jsMainImplementation("com.soywiz.korlibs.klock:klock")
    jsMainImplementation("com.zegreatrob.jsmints:minreact")
    jsMainImplementation("com.zegreatrob.jsmints:react-data-loader")
    jsMainImplementation("com.zegreatrob.testmints:action")
    jsMainImplementation("com.zegreatrob.testmints:action-async")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-css")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    jsMainImplementation("io.ktor:ktor-client-core")
    jsMainImplementation("io.ktor:ktor-client-content-negotiation")
    jsMainImplementation("io.ktor:ktor-serialization-kotlinx-json")
    jsMainImplementation(npmConstrained("@auth0/auth0-react"))
    jsMainImplementation(npmConstrained("blueimp-md5"))
    jsMainImplementation(npmConstrained("core-js"))
    jsMainImplementation(npmConstrained("css-loader"))
    jsMainImplementation(npmConstrained("d3"))
    jsMainImplementation(npmConstrained("d3-color"))
    jsMainImplementation(npmConstrained("d3-selection"))
    jsMainImplementation(npmConstrained("date-fns"))
    jsMainImplementation(npmConstrained("drag-drop-webkit-mobile"))
    jsMainImplementation(npmConstrained("favicons"))
    jsMainImplementation(npmConstrained("file-loader"))
    jsMainImplementation(npmConstrained("fitty"))
    jsMainImplementation(npmConstrained("html-webpack-harddisk-plugin"))
    jsMainImplementation(npmConstrained("html-webpack-plugin"))
    jsMainImplementation(npmConstrained("karma"))
    jsMainImplementation(npmConstrained("mini-css-extract-plugin"))
    jsMainImplementation(npmConstrained("raw-loader"))
    jsMainImplementation(npmConstrained("react-dnd"))
    jsMainImplementation(npmConstrained("react-dnd-html5-backend"))
    jsMainImplementation(npmConstrained("react-flip-toolkit"))
    jsMainImplementation(npmConstrained("react-use-websocket"))
    jsMainImplementation(npmConstrained("reactjs-popup"))
    jsMainImplementation(npmConstrained("style-loader"))
    jsMainImplementation(npmConstrained("styled-components"))
    jsMainImplementation(npmConstrained("text-loader"))
    jsMainImplementation(npmConstrained("url-loader"))
    jsMainImplementation(npmConstrained("webpack"))
    jsMainImplementation(npmConstrained("webpack-cli"))
    jsMainImplementation(npmConstrained("webpack-favicons"))

    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation(project(":libraries:test-logging"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:minspy")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test-common")
    jsTestImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

val taggerExtension = TaggerExtension.apply(rootProject)

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

rootProject.yarn.ignoreScripts = false

tasks {
    val cdnBuildOutput = "${project.buildDir.absolutePath}/cdn.json"
    val lookupCdnUrls by registering(NodeExec::class) {
        setup(project)
        dependsOn(cdnLookupConfiguration, "jsPublicPackageJson", ":kotlinNpmInstall")
        inputs.files(cdnLookupConfiguration)
        inputs.files(jsRuntimeClasspath)
        val settingsFile = File(project.projectDir, "cdn.settings.json")
        inputs.file(settingsFile)
        val settings = ObjectMapper().readTree(settingsFile)

        val cdnLookupFile = cdnLookupConfiguration.resolve().first()

        arguments = listOf(cdnLookupFile.absolutePath) + settings.fieldNames().asSequence().toList()
        val cdnOutputFile = file(cdnBuildOutput)
        outputFile = cdnOutputFile
        outputs.cacheIf { true }
    }
    val projectResultPath = "${rootProject.buildDir.path}/test-output/${project.path}/results".replace(":", "/")
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

    val browserProductionWebpack = named("jsBrowserProductionWebpack", KotlinWebpack::class) {
        dependsOn(lookupCdnUrls, jsProcessResources)
        inputs.file(cdnBuildOutput)
        inputs.file(File(project.projectDir, "cdn.settings.json"))
        outputs.dir(outputDirectory.dir("html"))
        outputs.file(outputDirectory.file("client-vendor.js"))
        outputs.file(outputDirectory.file("client-kotlin.js"))
        outputs.file(outputDirectory.file("client-coupling-core.js"))
        outputs.file(outputDirectory.file("client-kotlinx.js"))
        outputs.file(outputDirectory.file("client-ktor.js"))
        outputs.cacheIf { true }
    }

    val uploadToS3 by registering(Exec::class) {
        dependsOn(browserProductionWebpack)
        if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.get().outputDirectory.get().asFile.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${rootProject.version}".split(" ")
    }
    taggerExtension.releaseProvider.configure {
        finalizedBy(uploadToS3)
    }

    val additionalResources by registering(Copy::class) {
        outputs.cacheIf { true }
        dependsOn(":sdk:jsProcessResources")
        into("${project.buildDir.absolutePath}/additionalResources")
        from(provider { (findByPath(":sdk:jsProcessResources") as ProcessResources).destinationDir })
    }
    jsProcessResources {
        dependsOn(additionalResources)
    }

    named("jsBrowserTest") {
        outputs.cacheIf { true }
    }
}

tasks {
    formatKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinJsMain {
        dependsOn("kspKotlinJs")
    }
    withType(FormatTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
    withType(LintTask::class) {
        exclude { spec -> spec.file.absolutePath.contains("generated") }
    }
}

artifacts {
    add(clientConfiguration.name, tasks.compileProductionExecutableKotlinJs.map { it.destinationDirectory }) {
        builtBy(tasks.compileProductionExecutableKotlinJs)
    }
    val browserProductionWebpack = tasks.named("jsBrowserProductionWebpack", KotlinWebpack::class)
    val browserDistribution = tasks.named("jsBrowserDistribution")
    add(clientConfiguration.name, browserProductionWebpack.map { it.outputDirectory }) {
        builtBy(browserProductionWebpack, browserDistribution)
    }
}
