import com.fasterxml.jackson.databind.ObjectMapper
import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.serialization")
}

kotlin {
    js {
        browser {
            webpackTask {
                dependsOn("additionalResources")
                inputs.files("${project.projectDir}/src/main/resources")
                val profile: String? by project
                if (!profile.isNullOrBlank()) {
                    this.args.add("--profile")
                    this.args.add("--json=$buildDir/reports/stats.json")
                }
            }
        }
    }
    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }
    }
}

val clientConfiguration: Configuration by configurations.creating
val cdnLookupConfiguration: Configuration by configurations.creating

dependencies {
    cdnLookupConfiguration(
        project(mapOf("path" to ":coupling-libraries:cdnLookup", "configuration" to "cdnLookupConfiguration"))
    )

    implementation(kotlin("stdlib-js"))
    implementation(project(":sdk"))
    implementation(project(":coupling-libraries:components"))
    implementation(project(":coupling-libraries:model"))
    implementation(project(":coupling-libraries:action"))
    implementation(project(":coupling-libraries:logging"))
    implementation(project(":coupling-libraries:repository-memory"))
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    implementation("com.soywiz.korlibs.klock:klock:2.7.0")
    implementation("com.benasher44:uuid:0.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0")

    testImplementation(project(":coupling-libraries:stub-model"))
    testImplementation(project(":coupling-libraries:test-react"))
    testImplementation(project(":coupling-libraries:test-logging"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.zegreatrob.testmints:minspy")
    testImplementation("com.zegreatrob.jsmints:minenzyme")
}

val taggerExtension = com.zegreatrob.coupling.plugins.tagger.TaggerExtension.apply(rootProject)

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
    val cdnBuildOutput = "${project.buildDir.absolutePath}/cdn.json"
    val lookupCdnUrls by registering(NodeExec::class) {
        setup(project)
        dependsOn(cdnLookupConfiguration, "publicPackageJson")
        inputs.files(cdnLookupConfiguration)
        val settingsFile = File(project.projectDir, "cdn.settings.json")
        inputs.file(settingsFile)
        val settings = ObjectMapper().readTree(settingsFile)

        val cdnLookupFile = cdnLookupConfiguration.resolve().first()

        arguments = listOf(cdnLookupFile.absolutePath) + settings.fieldNames().asSequence().toList()
        val cdnOutputFile = file(cdnBuildOutput)
        outputFile = cdnOutputFile
        outputs.cacheIf { true }
    }

    named("compileTestDevelopmentExecutableKotlinJs") {
        dependsOn(lookupCdnUrls)
    }
    compileProductionExecutableKotlinJs {}

    val browserProductionWebpack = named("browserProductionWebpack", KotlinWebpack::class) {
        dependsOn(lookupCdnUrls)
        inputs.file(cdnBuildOutput)
        inputs.file(File(project.projectDir, "cdn.settings.json"))
        outputs.dir(File(destinationDirectory, "html"))
        outputs.file(File(destinationDirectory, "client-vendor.js"))
        outputs.file(File(destinationDirectory, "client-kotlin.js"))
        outputs.file(File(destinationDirectory, "client-coupling-core.js"))
        outputs.file(File(destinationDirectory, "client-kotlinx.js"))
        outputs.file(File(destinationDirectory, "client-ktor.js"))
        outputs.cacheIf { true }
    }

    val uploadToS3 by registering(Exec::class) {
        dependsOn(browserProductionWebpack)
        if (("${rootProject.version}").contains("SNAPSHOT")) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.get().destinationDirectory.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${rootProject.version}".split(" ")
    }
    taggerExtension.releaseProvider.configure {
        finalizedBy(uploadToS3)
    }

    val additionalResources by registering(Copy::class) {
        outputs.cacheIf { true }
        val javascriptConfig = configurations["runtimeClasspath"]
        dependsOn(javascriptConfig)
        duplicatesStrategy = DuplicatesStrategy.WARN
        val additionalResourcesPath = "${project.buildDir.absolutePath}/additionalResources"
        into(additionalResourcesPath)
        from({
            javascriptConfig.files.map {
                if (!it.isFile || !it.name.endsWith(".klib")) {
                    null
                } else {
                    zipTree(it).matching {
                        exclude(
                            "default",
                            "default/**/*",
                            "kotlin",
                            "kotlin/**/*",
                            "kotlin-test",
                            "kotlin-test/**/*",
                            "META-INF",
                            "META-INF/**/*",
                            "org",
                            "org/**/*",
                            "kotlin.js",
                            "kotlin.js.map",
                            "kotlin.meta.js",
                            "kotlin-test.js",
                            "kotlin-test.js.map",
                            "kotlin-test.meta.js",
                            "package.json",
                        )
                    }
                }
            }
        })
    }

    named("browserTest") {
        outputs.cacheIf { true }
    }
}

artifacts {
    add(clientConfiguration.name, tasks.compileProductionExecutableKotlinJs.map { it.outputFileProperty }) {
        builtBy(tasks.compileProductionExecutableKotlinJs)
    }
    val browserProductionWebpack = tasks.named("browserProductionWebpack", KotlinWebpack::class)
    val browserDistribution = tasks.named("browserDistribution")
    add(clientConfiguration.name, browserProductionWebpack.map { it.destinationDirectory }) {
        builtBy(browserProductionWebpack, browserDistribution)
    }
}
