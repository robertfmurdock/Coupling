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
        getByName("main") {
            resources.srcDir("src/main/javascript")
        }
    }
}

val runtimeClasspath: Configuration by configurations.getting
val clientConfiguration: Configuration by configurations.creating
val cdnLookupConfiguration: Configuration by configurations.creating

dependencies {
    cdnLookupConfiguration(
        project(mapOf("path" to ":scripts:cdn-lookup", "configuration" to "cdnLookupConfiguration"))
    )
    implementation(kotlin("stdlib-js"))
    implementation(project("components"))
    implementation(project(":sdk"))
    implementation(project(":libraries:model"))
    implementation(project(":libraries:json"))
    implementation(project(":libraries:action"))
    implementation(project(":libraries:logging"))
    implementation(project(":libraries:repository:core"))
    implementation(project(":libraries:repository:memory"))
    implementation("com.benasher44:uuid")
    implementation("com.soywiz.korlibs.klock:klock")
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation(npmConstrained("@auth0/auth0-react"))
    implementation(npmConstrained("blueimp-md5"))
    implementation(npmConstrained("core-js"))
    implementation(npmConstrained("css-loader"))
    implementation(npmConstrained("d3"))
    implementation(npmConstrained("d3-color"))
    implementation(npmConstrained("d3-selection"))
    implementation(npmConstrained("date-fns"))
    implementation(npmConstrained("drag-drop-webkit-mobile"))
    implementation(npmConstrained("favicons"))
    implementation(npmConstrained("file-loader"))
    implementation(npmConstrained("fitty"))
    implementation(npmConstrained("html-webpack-harddisk-plugin"))
    implementation(npmConstrained("html-webpack-plugin"))
    implementation(npmConstrained("karma"))
    implementation(npmConstrained("mini-css-extract-plugin"))
    implementation(npmConstrained("raw-loader"))
    implementation(npmConstrained("react-dnd"))
    implementation(npmConstrained("react-dnd-html5-backend"))
    implementation(npmConstrained("react-flip-toolkit"))
    implementation(npmConstrained("react-use-websocket"))
    implementation(npmConstrained("reactjs-popup"))
    implementation(npmConstrained("style-loader"))
    implementation(npmConstrained("styled-components"))
    implementation(npmConstrained("text-loader"))
    implementation(npmConstrained("url-loader"))
    implementation(npmConstrained("webpack"))
    implementation(npmConstrained("webpack-cli"))
    implementation(npmConstrained("webpack-favicons"))

    testImplementation(project(":libraries:stub-model"))
    testImplementation(project(":libraries:test-logging"))
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.zegreatrob.testmints:minspy")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
}

val taggerExtension = TaggerExtension.apply(rootProject)

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

rootProject.yarn.ignoreScripts = false

tasks {
    val cdnBuildOutput = "${project.buildDir.absolutePath}/cdn.json"
    val lookupCdnUrls by registering(NodeExec::class) {
        setup(project)
        dependsOn(cdnLookupConfiguration, "publicPackageJson", ":kotlinNpmInstall")
        inputs.files(cdnLookupConfiguration)
        inputs.files(runtimeClasspath)
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

    val browserProductionWebpack = named("browserProductionWebpack", KotlinWebpack::class) {
        dependsOn(lookupCdnUrls, "processResources")
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
    named("processResources") {
        dependsOn(additionalResources)
    }

    named("browserTest") {
        outputs.cacheIf { true }
    }
}

tasks {
    formatKotlinMain {
        dependsOn("kspKotlinJs")
    }
    lintKotlinMain {
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
    val browserProductionWebpack = tasks.named("browserProductionWebpack", KotlinWebpack::class)
    val browserDistribution = tasks.named("browserDistribution")
    add(clientConfiguration.name, browserProductionWebpack.map { it.outputDirectory }) {
        builtBy(browserProductionWebpack, browserDistribution)
    }
}
