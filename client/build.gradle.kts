import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization") version "1.6.0"
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
}

kotlin {
    js {
        useCommonJs()
        browser()
        binaries.executable()
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }
    }
}

val packageJson = loadPackageJson()

val clientConfiguration: Configuration by configurations.creating

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":model"))
    implementation(project(":sdk"))
    implementation(project(":action"))
    implementation(project(":logging"))
    implementation(project(":repository-memory"))
    implementation("com.zegreatrob.testmints:minreact:5.3.3")
    implementation("com.zegreatrob.testmints:react-data-loader:5.3.3")
    implementation("com.zegreatrob.testmints:action:5.3.3")
    implementation("com.zegreatrob.testmints:action-async:5.3.3")
    implementation("com.soywiz.korlibs.klock:klock:2.4.8")
    implementation("com.benasher44:uuid:0.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.276-kotlin-1.6.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.276-kotlin-1.6.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-pre.276-kotlin-1.6.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.276-kotlin-1.6.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.276-kotlin-1.6.0")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.0.2-pre.276-kotlin-1.6.0")

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    testImplementation("com.zegreatrob.testmints:minenzyme:5.3.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:5.3.0")
    testImplementation("com.zegreatrob.testmints:async:5.3.0")
    testImplementation("com.zegreatrob.testmints:minassert:5.3.0")
    testImplementation("com.zegreatrob.testmints:minspy:5.3.0")
}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class)
    val browserProductionWebpack by getting(KotlinWebpack::class)
    val browserDistribution by getting {}

    artifacts {
        add(clientConfiguration.name, compileProductionExecutableKotlinJs.outputFile) {
            builtBy(compileProductionExecutableKotlinJs)
        }
        add(clientConfiguration.name, browserProductionWebpack.destinationDirectory) {
            builtBy(browserProductionWebpack, browserDistribution)
        }
    }
    create<Exec>("uploadToS3") {
        dependsOn(browserProductionWebpack)
        if (version.toString().contains("SNAPSHOT")) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.destinationDirectory.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${version}".split(" ")
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
