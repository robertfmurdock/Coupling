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
    }
}

val packageJson = loadPackageJson()

dependencies {
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.148-kotlin-1.4.30")
    implementation("com.zegreatrob.testmints:wdio:3.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    testImplementation(project(":sdk"))
    testImplementation(project(":test-logging"))
    testImplementation(kotlin("test-js"))
    testImplementation("io.github.microutils:kotlin-logging:2.0.3")
    testImplementation("com.zegreatrob.testmints:standard:3.2.2")
    testImplementation("com.zegreatrob.testmints:minassert:3.2.3")
    testImplementation("com.zegreatrob.testmints:async:3.2.3")
    packageJson.dependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class)
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class)

    val wdioRun by creating(Exec::class) {
        mustRunAfter(":client:check")
        configureWdioRun(
            compileKotlinJs = compileKotlinJs,
            pathToNodeApp = "${project(":server").buildDir.absolutePath}/executable/app.js",
            wdioConfig = project.projectDir.resolve("wdio.conf.js"),
            webpackConfig = project.projectDir.resolve("webpack.config.js"),
            webpackedWdioConfigOutput = "config"
        )
        dependsOn(compileKotlinJs, compileTestKotlinJs)
        inputs.files(compileKotlinJs.outputs.files)
        inputs.files(compileTestKotlinJs.outputs.files)
        dependsOn(":server:assemble")
        environment("PORT" to "3099")
    }

    val check by getting {
        dependsOn(wdioRun)
    }
}
