
import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.ProjectLocalConfigurations
import org.jetbrains.kotlin.gradle.targets.js.KotlinJsCompilerAttribute
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
        useCommonJs()
        binaries.executable()
        compilations {
            val e2eTest by creating
            binaries.executable(e2eTest)
        }
    }
}

val packageJson = loadPackageJson()

val appConfiguration: Configuration by configurations.creating {
    attributes {
        attribute(KotlinJsCompilerAttribute.jsCompilerAttribute, KotlinJsCompilerAttribute.ir)
        attribute(ProjectLocalConfigurations.ATTRIBUTE, ProjectLocalConfigurations.PUBLIC_VALUE)
        attribute(KotlinPlatformType.attribute, KotlinPlatformType.js)
    }
}

val testLoggingLib: Configuration by configurations.creating {
}

kotlin {
    sourceSets {
        val e2eTest by getting {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":test-logging"))
                implementation(kotlin("test-js"))
                implementation("io.github.microutils:kotlin-logging:2.0.6")
                implementation("com.zegreatrob.testmints:standard:4.0.7")
                implementation("com.zegreatrob.testmints:minassert:4.0.7")
                implementation("com.zegreatrob.testmints:async:4.0.7")
                implementation("com.zegreatrob.testmints:wdio:4.0.7")
                implementation(appConfiguration)
                packageJson.devDependencies().forEach {
                    implementation(npm(it.first, it.second.asText()))
                }
            }
        }
    }
}

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    testLoggingLib(project(mapOf("path" to ":test-logging", "configuration" to "testLoggingLib")))
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("com.benasher44:uuid:0.3.0")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.148-kotlin-1.4.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
    implementation("com.zegreatrob.testmints:wdio:4.0.7")
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    packageJson.devDependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }
}

tasks {
    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileE2eTestProductionExecutableKotlinJs by getting {}
    val productionExecutableCompileSync by getting {}

    val pathToNodeApp = "${project(":server").buildDir.absolutePath}/executable/app.js"
    val wdioConfig = project.projectDir.resolve("wdio.conf.js")
    val webpackConfig = project.projectDir.resolve("webpack.config.js")
    val webpackedWdioConfigOutput = "config"

    val nodeRun by getting(NodeJsExec::class) {
        dependsOn(
            compileProductionExecutableKotlinJs,
            productionExecutableCompileSync,
            compileE2eTestProductionExecutableKotlinJs,
            appConfiguration,
            testLoggingLib
        )
        inputs.files(compileProductionExecutableKotlinJs.outputs.files)
        inputs.files(compileE2eTestProductionExecutableKotlinJs.outputs.files)
        inputs.files(project.file(pathToNodeApp).parent)
        inputs.files(wdioConfig)
        outputs.dir("${project.buildDir}/reports/e2e")

        environment("PORT" to "3099")
        environment(
            mapOf(
                "APP_PATH" to pathToNodeApp,
                "NODE_PATH" to "${project.rootProject.buildDir.path}/js/node_modules",
                "BUILD_DIR" to project.buildDir.absolutePath,
                "WEBPACK_CONFIG" to webpackConfig,
                "WEBPACKED_WDIO_CONFIG_OUTPUT" to webpackedWdioConfigOutput
            )
        )
        val logFile = project.file("build/logs/run.log")
        logFile.parentFile.mkdirs()
        standardOutput = logFile.outputStream()
    }

    val check by getting {
        dependsOn(nodeRun)
    }
}
