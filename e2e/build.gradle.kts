import com.zegreatrob.coupling.build.configureWdioRun
import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    js {
        nodejs { testTask { enabled = false } }
        useCommonJs()
        binaries.executable()
    }
}

val packageJson = loadPackageJson()

val appConfiguration: Configuration by configurations.creating

dependencies {
    appConfiguration(project(mapOf("path" to ":server", "configuration" to "appConfiguration")))
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("com.benasher44:uuid:0.3.0")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.148-kotlin-1.4.30")
    implementation("com.zegreatrob.testmints:wdio:4.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    testImplementation(project(":sdk"))
    testImplementation(project(":test-logging"))
    testImplementation(kotlin("test-js"))
    testImplementation("io.github.microutils:kotlin-logging:2.0.6")
    testImplementation("com.zegreatrob.testmints:standard:4.0.7")
    testImplementation("com.zegreatrob.testmints:minassert:4.0.7")
    testImplementation("com.zegreatrob.testmints:async:4.0.7")
    packageJson.dependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
}

tasks {
    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}
    val compileTestProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}
    val assemble by getting {}

    val wdioRun by creating(Exec::class) {
        configureWdioRun(
            compileKotlinJs = compileProductionExecutableKotlinJs,
            pathToNodeApp = "${project(":server").buildDir.absolutePath}/executable/app.js",
            wdioConfig = project.projectDir.resolve("wdio.conf.js"),
            webpackConfig = project.projectDir.resolve("webpack.config.js"),
            webpackedWdioConfigOutput = "config"
        )
        dependsOn(compileProductionExecutableKotlinJs, compileTestProductionExecutableKotlinJs)
        inputs.files(compileProductionExecutableKotlinJs.outputs.files)
        inputs.files(compileTestProductionExecutableKotlinJs.outputs.files)
        dependsOn(appConfiguration)
        environment("PORT" to "3099")
    }

    val check by getting {
        dependsOn(wdioRun)
    }
}
