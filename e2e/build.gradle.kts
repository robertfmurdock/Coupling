import com.zegreatrob.coupling.build.BuildConstants.testmintsVersion
import com.zegreatrob.coupling.build.configureWdioRun
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

dependencies {
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.110-kotlin-1.4.0")
    implementation("com.zegreatrob.testmints:wdio:$testmintsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation(npm("@log4js-node/log4js-api", "1.0.2"))
    implementation(npm("@rpii/wdio-html-reporter", "6.1.1"))
    implementation(npm("@wdio/cli", "6.4.0"))
    implementation(npm("@wdio/dot-reporter", "6.4.0"))
    implementation(npm("@wdio/jasmine-framework", "6.4.0"))
    implementation(npm("@wdio/local-runner", "6.4.0"))
    implementation(npm("chromedriver", "84.0.1"))
    implementation(npm("fs-extra", "9.0.1"))
    implementation(npm("webpack", "4.44.1"))
    implementation(npm("webpack-node-externals", "2.5.1"))
    implementation(npm("wdio-chromedriver-service", "6.0.3"))
    implementation(npm("css-loader", "4.2.1"))
    implementation(npm("url-loader", "4.1.0"))

    testImplementation(project(":sdk"))
    testImplementation(project(":test-logging"))

    testImplementation(kotlin("test-js"))
    testImplementation(npm("axios-cookiejar-support", "^0.5.0"))
    testImplementation(npm("tough-cookie", "^3.0.1"))
    testImplementation(npm("uuid", "^3.3.2"))
    testImplementation("io.github.microutils:kotlin-logging-js:1.8.3")
    testImplementation("com.zegreatrob.testmints:standard:$testmintsVersion")
    testImplementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
    testImplementation("com.zegreatrob.testmints:async:+")
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

