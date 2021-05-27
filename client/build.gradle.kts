import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.5.0"
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
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    implementation("com.zegreatrob.testmints:minreact:4.0.12")
    implementation("com.zegreatrob.testmints:react-data-loader:4.0.12")
    implementation("com.zegreatrob.testmints:action:4.0.12")
    implementation("com.zegreatrob.testmints:action-async:4.0.12")
    implementation("com.soywiz.korlibs.klock:klock:2.1.0")
    implementation("com.benasher44:uuid:0.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-styled:5.2.1-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-react:17.0.1-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.148-kotlin-1.4.21")
    implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-pre.148-kotlin-1.4.21")

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    testImplementation("com.zegreatrob.testmints:minenzyme:4.0.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:4.0.12")
    testImplementation("com.zegreatrob.testmints:async:4.0.12")
    testImplementation("com.zegreatrob.testmints:minassert:4.0.12")
    testImplementation("com.zegreatrob.testmints:minspy:4.0.12")
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
        if (version.toString().contains("SNAPSHOT")) {
            enabled = false
        }
        val absolutePath = browserProductionWebpack.destinationDirectory.absolutePath
        commandLine = "aws s3 sync $absolutePath s3://assets.zegreatrob.com/coupling/${version}".split(" ")
    }

    val dependencyResources by creating(Copy::class) {
        dependsOn(configurations["jsDefault"])
        duplicatesStrategy = DuplicatesStrategy.WARN
        into("$buildDir/processedResources/js/main")
        from({
            configurations["jsDefault"].files.map {
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
