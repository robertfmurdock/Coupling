import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.serialization")
}

kotlin {
    js {
        browser {
            webpackTask {
                inputs.files("${project.projectDir}/src/main/resources")
                val profile: String? by project
                if(!profile.isNullOrBlank()) {
                    this.args.add("--profile")
                    this.args.add("--json=${buildDir}/reports/stats.json")
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
    implementation("com.zegreatrob.coupling.libraries:model")
    implementation("com.zegreatrob.coupling.libraries:action")
    implementation("com.zegreatrob.coupling.libraries:logging")
    implementation("com.zegreatrob.coupling.libraries:repository-memory")
    implementation("com.zegreatrob.testmints:action")
    implementation("com.zegreatrob.testmints:action-async")
    implementation("com.zegreatrob.jsmints:minreact")
    implementation("com.zegreatrob.jsmints:react-data-loader")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-popper")
    implementation("com.soywiz.korlibs.klock:klock:2.5.2")
    implementation("com.benasher44:uuid:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")

    testImplementation("com.zegreatrob.coupling.libraries:stub-model")
    testImplementation("com.zegreatrob.coupling.libraries:test-logging")
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

    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class)
    val browserProductionWebpack by getting(KotlinWebpack::class)
    val browserDistribution by getting {}

    artifacts {
        add(clientConfiguration.name, compileProductionExecutableKotlinJs.outputFileProperty) {
            builtBy(compileProductionExecutableKotlinJs)
        }
        add(clientConfiguration.name, browserProductionWebpack.destinationDirectory) {
            builtBy(browserProductionWebpack, browserDistribution)
        }
    }
    val uploadToS3 = create<Exec>("uploadToS3") {
        dependsOn(browserProductionWebpack)
        if (version.toString().contains("SNAPSHOT")) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.destinationDirectory.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${version}".split(" ")
    }
    findByPath(":release")!!.finalizedBy(uploadToS3)

    "browserProductionWebpack" {
        outputs.cacheIf { true }
    }

    val dependencyResources by creating(Copy::class) {
        val javascriptConfig = configurations["runtimeClasspath"]
        dependsOn(javascriptConfig)
        duplicatesStrategy = DuplicatesStrategy.WARN
        into("$buildDir/processedResources/js/main")
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

    val processResources by getting {
        dependsOn(dependencyResources)
    }
}
