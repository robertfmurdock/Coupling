
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.io.FileOutputStream

plugins {
    kotlin("js")
    id("kotlin-dce-js")
    id("com.github.node-gradle.node")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

kotlin {
    target {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":model"))
    implementation(project(":json"))
    implementation(project(":sdk"))
    implementation(project(":action"))
    implementation(project(":logging"))
    implementation("com.soywiz.korlibs.klock:klock:1.8.1")
    implementation("io.github.microutils:kotlin-logging-js:1.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0-1.3.60-eap-76")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.89-kotlin-1.3.60")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.89-kotlin-1.3.60")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.89-kotlin-1.3.60")
    implementation("org.jetbrains:kotlin-react:16.9.0-pre.89-kotlin-1.3.60")
    implementation("org.jetbrains:kotlin-react-dom:16.9.0-pre.89-kotlin-1.3.60")
    implementation("org.jetbrains:kotlin-react-router-dom:4.3.1-pre.89-kotlin-1.3.60")

    testImplementation(project(":test-logging"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:async-js:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {

    val browserWebpack by getting {
        enabled = false
    }

    val clean by getting {
        doLast {
            delete(file("build"))
        }
    }

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val runDceKotlin by getting(KotlinJsDce::class) {
    }

    val runDceTestKotlin by getting(KotlinJsDce::class) {
        keep(
            "client_test.setLogLevel"
        )
    }

    val vendorCompile by creating(YarnTask::class) {
        dependsOn(yarn, runDceKotlin, compileKotlinJs)
        mustRunAfter("clean")

        inputs.files(runDceKotlin.outputs)
        inputs.files("node_modules")
        inputs.file(file("package.json"))
        inputs.files("${rootProject.buildDir.path}/js/node_modules")
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("build/lib/vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    val testVendorCompile by creating(YarnTask::class) {
        dependsOn(yarn, runDceKotlin, compileKotlinJs, compileTestKotlinJs)
        mustRunAfter("clean")

        inputs.files(runDceKotlin.outputs)
        inputs.files("node_modules")
        inputs.file(file("package.json"))
        inputs.files("${rootProject.buildDir.path}/js/node_modules")
        inputs.file(file("test/vendor.webpack.config.js"))
        outputs.dir("build/lib/test-vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "test/vendor.webpack.config.js")
    }

    task<YarnTask>("compile") {
        dependsOn(yarn, vendorCompile, runDceKotlin, processResources)
        inputs.dir("node_modules").skipWhenEmpty()
        inputs.file(file("package.json"))
        inputs.files(runDceKotlin.outputs)
        inputs.file(file("webpack.config.js"))
        inputs.file(file("tsconfig.json"))
        inputs.files("build/lib/vendor")
        inputs.files("build/processedResources")
        outputs.dir("build/lib/main")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    val karma by creating(YarnTask::class) {
        dependsOn(
            yarn,
            vendorCompile,
            testVendorCompile,
            ":action:jsTest",
            compileTestKotlinJs,
            runDceTestKotlin
        )
        inputs.file(file("package.json"))
        inputs.files(vendorCompile.inputs.files)
        inputs.dir("build/kotlin-js-min/main")
        inputs.dir("build/kotlin-js-min/test")
        inputs.dir("test")
        outputs.dir(file("build/test-results"))

        args = listOf("run", "test", "--silent")
    }

    val updateDependencies by creating(YarnTask::class) {
        dependsOn(yarn)
        args = listOf("run", "ncu", "-u")
    }

    val test by getting {
        dependsOn(karma)
    }

    task<YarnTask>("testWatch") {
        args = listOf("run", "testWatch")
    }

    task<YarnTask>("stats") {
        dependsOn(yarn, vendorCompile)

        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("-s", "webpack", "--json", "--profile", "--config", "webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/stats.json"))
        })
    }

    task<YarnTask>("testStats") {
        dependsOn(yarn, testVendorCompile)

        setEnvironment(mapOf("NODE_ENV" to nodeEnv))

        args = listOf("-s", "webpack", "--json", "--profile", "--config", "test/webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/test-stats.json"))
        })
    }

    task<YarnTask>("vendorStats") {
        dependsOn(yarn, runDceKotlin)
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("-s", "webpack", "--json", "--profile", "--config", "vendor.webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/vendor.stats.json"))
        })
    }

    forEach { if (!it.name.startsWith("clean")) it.mustRunAfter("clean") }

}
