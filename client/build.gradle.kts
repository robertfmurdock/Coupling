import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import com.zegreatrob.coupling.build.forEachJsTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce
import java.io.FileOutputStream

plugins {
    id("kotlin2js")
    id("kotlin-dce-js")
    id("com.github.node-gradle.node")
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
    implementation(project(":logging"))
    implementation("com.soywiz:klock:1.1.1")
    implementation("io.github.microutils:kotlin-logging-js:1.7.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.13.0")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.83-kotlin-1.3.50")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.83-kotlin-1.3.50")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.83-kotlin-1.3.50")
    implementation("org.jetbrains:kotlin-react:16.9.0-pre.83-kotlin-1.3.50")
    implementation("org.jetbrains:kotlin-react-dom:16.9.0-pre.83-kotlin-1.3.50")
    implementation("org.jetbrains:kotlin-react-router-dom:4.3.1-pre.83-kotlin-1.3.50")

    testImplementation(project(":test-logging"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:async-js:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
}

sourceSets {
    val main by getting {
        resources { srcDir("src/main/javascript") }
    }
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
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlin2Js by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val unpackJsGradleDependencies by creating(UnpackGradleDependenciesTask::class) {
        inputs.files(compileKotlin2Js.inputs.files)
        dependsOn(":engine:assemble", ":test-logging:assemble")

        forEachJsTarget(project).let { (main, test) ->
            customCompileConfiguration = main
            customTestCompileConfiguration = test
        }
    }

    val runDceKotlinJs by getting(KotlinJsDce::class) {
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
        inputs.dir("build/node_modules_imported")
        inputs.file(file("vendor.webpack.config.js"))
        outputs.dir("build/lib/vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "vendor.webpack.config.js")
    }

    val testVendorCompile by creating(YarnTask::class) {
        dependsOn(yarn, runDceKotlinJs, unpackJsGradleDependencies)
        mustRunAfter("clean")

        inputs.files(runDceKotlinJs.outputs)
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.dir("build/node_modules_imported")
        inputs.file(file("test/vendor.webpack.config.js"))
        outputs.dir("build/lib/test-vendor")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "test/vendor.webpack.config.js")
    }

    task<YarnTask>("compile") {
        dependsOn(yarn, vendorCompile, runDceKotlinJs, processResources)
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.files(runDceKotlinJs.outputs)
        inputs.file(file("webpack.config.js"))
        inputs.file(file("tsconfig.json"))
        inputs.dir("build/lib/vendor")
        inputs.dir("build/resources")
        outputs.dir("build/lib/main")
        setEnvironment(mapOf("NODE_ENV" to nodeEnv))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    val karma by creating(YarnTask::class) {
        dependsOn(
                yarn,
                vendorCompile,
                testVendorCompile,
                ":commonKt:jsTest",
                compileTestKotlin2Js,
                runDceTestKotlinJs,
                unpackJsGradleDependencies
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