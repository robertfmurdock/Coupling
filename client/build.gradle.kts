
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.io.FileOutputStream

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("com.github.node-gradle.node")
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":commonKt"))
    implementation("com.soywiz:klock:1.1.1")
    implementation("io.github.microutils:kotlin-logging-js:1.6.24")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.10.0")
}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
    val yarn by getting {
        mustRunAfter(":commonKt:yarn")
    }

    val clean by getting {
        doLast {
            delete(file("build"))
        }
    }

    val compileKotlin2Js by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlin2Js by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val runDceKotlinJs by getting(KotlinJsDce::class) {
        keep(
                "commonKt.historyFromArray",
                "commonKt.com.zegreatrob.coupling.common.ComposeStatisticsActionDispatcher",
                "commonKt.com.zegreatrob.coupling.common.initializeLogging",
                "client.performComposeStatisticsAction"
        )
    }

    val runDceTestKotlinJs by getting(KotlinJsDce::class) {
        keep(
                "client_test.setLogLevel"
        )
    }

    val vendorCompile by creating(YarnTask::class) {
        dependsOn(yarn, runDceKotlinJs)
        mustRunAfter("clean")

        inputs.files(runDceKotlinJs.outputs)
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("build/lib/vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    task<YarnTask>("compile") {
        dependsOn(yarn, vendorCompile, runDceKotlinJs)
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("yarn.lock"))
        inputs.file(file("webpack.config.js"))
        inputs.file(file("tsconfig.json"))
        inputs.dir("../common")
        inputs.dir("./app")
        inputs.dir("./images")
        inputs.dir("./stylesheets")
        outputs.dir("build/lib")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    val karma by creating(YarnTask::class) {
        dependsOn(yarn, vendorCompile, ":commonKt:jsTest", runDceTestKotlinJs)
        inputs.file(file("package.json"))
        inputs.files(vendorCompile.inputs.files)
        inputs.dir("test")
        outputs.dir(file("build/test-results"))

        args = listOf("run", "test", "--silent")
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

    task<YarnTask>("vendorStats") {
        dependsOn(yarn, runDceKotlinJs)
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("-s", "webpack", "--json", "--profile", "--config", "vendor.webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/vendor.stats.json"))
        })
    }

    forEach { if (!it.name.startsWith("clean")) it.mustRunAfter("clean") }

}