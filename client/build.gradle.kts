
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
        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}

val clientConfiguration: Configuration by configurations.creating

dependencies {
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
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.5")

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

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
    val cdnBuildOutput = "${project.buildDir.absolutePath}/cdn.json"
    val lookupCdnUrls by registering(NodeExec::class) {
        dependsOn(":coupling-libraries:cdnLookup:compileProductionExecutableKotlinJs")
        val cdnLookupFile = project.rootDir.absolutePath +
            "/coupling-libraries/cdnLookup/build/compileSync/main/productionExecutable/kotlin/Coupling-cdnLookup.js"
        inputs.file(cdnLookupFile)
        val settingsFile = File(project.projectDir, "cdn.settings.json")
        inputs.file(settingsFile)
        val settings = ObjectMapper().readTree(settingsFile)
        val cdnLibraries = settings.fieldNames()

        arguments = listOf(cdnLookupFile) + cdnLibraries.asSequence().toList()
        val cdnOutputFile = file(cdnBuildOutput)
        outputs.file(cdnBuildOutput)
        setup(project)
        outputFile = cdnOutputFile
        outputs.cacheIf { true }
    }

    named("compileTestDevelopmentExecutableKotlinJs") {
        dependsOn(lookupCdnUrls)
    }
    compileProductionExecutableKotlinJs {
    }
    val browserDistribution = named("browserDistribution")
    val browserProductionWebpack = named("browserProductionWebpack", KotlinWebpack::class) {
        dependsOn(lookupCdnUrls)
        inputs.file(cdnBuildOutput)
        outputs.dir(destinationDirectory.absolutePath + "/html")
        outputs.cacheIf { true }
        artifacts {
            add(clientConfiguration.name, destinationDirectory) {
                builtBy(this@named, browserDistribution)
            }
        }
    }

    val uploadToS3 by registering(Exec::class) {
        dependsOn(browserProductionWebpack, ":release")
        mustRunAfter(check, ":e2e:check")
        if (rootProject.version.toString().contains("SNAPSHOT")) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.get().destinationDirectory.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/$rootProject.version".split(" ")
    }
    val release by registering {
        dependsOn(":release", uploadToS3)
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

    artifacts {
        add(clientConfiguration.name, compileProductionExecutableKotlinJs.get().outputFileProperty) {
            builtBy(compileProductionExecutableKotlinJs)
        }
    }
}
