import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.io.FileOutputStream

plugins {
    kotlin("js")
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

val packageJson = loadPackageJson()

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":model"))
    implementation(project(":json"))
    implementation(project(":sdk"))
    implementation(project(":action"))
    implementation(project(":logging"))
    implementation("com.zegreatrob.testmints:action:+")
    implementation("com.zegreatrob.testmints:action-async:+")
    implementation("com.soywiz.korlibs.klock:klock:1.10.6")
    implementation("com.benasher44:uuid:0.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.107-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.107-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.107-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.109-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.109-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.107-kotlin-1.3.72")

    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:async:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
    testImplementation("com.zegreatrob.testmints:minspy:+")

}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {

    val clean by getting {
        doLast {
            delete(file("build/lib"))
            delete(file("build/report"))
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

    val karma by creating(YarnTask::class) {
        dependsOn(
            yarn,
            ":action:jsTest",
            compileTestKotlinJs
        )
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("test")
        inputs.file(compileKotlinJs.outputFile)
        inputs.file(compileTestKotlinJs.outputFile)
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

    task<YarnTask>("testStats") {
        dependsOn(yarn)

        setEnvironment(mapOf("NODE_ENV" to nodeEnv))

        args = listOf("-s", "webpack", "--json", "--profile", "--config", "test/webpack.config.js")

        setExecOverrides(closureOf<ExecSpec> {
            file("build/report").mkdirs()
            standardOutput = FileOutputStream(file("build/report/test-stats.json"))
        })
    }

    forEach { if (!it.name.startsWith("clean")) it.mustRunAfter("clean") }

}
