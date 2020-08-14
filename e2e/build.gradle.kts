import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    target {
        nodejs { testTask { enabled = false } }
        useCommonJs()
    }
}

dependencies {
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.110-kotlin-1.3.72")
    implementation("com.zegreatrob.testmints:wdio:+")
    implementation(npm("@log4js-node/log4js-api"))
    implementation(npm("@rpii/wdio-html-reporter"))
    implementation(npm("@wdio/cli"))
    implementation(npm("@wdio/dot-reporter"))
    implementation(npm("@wdio/jasmine-framework"))
    implementation(npm("@wdio/local-runner"))
    implementation(npm("chromedriver"))
    implementation(npm("fs-extra"))
    implementation(npm("webpack"))
    implementation(npm("webpack-node-externals"))
    implementation(npm("wdio-chromedriver-service"))
    implementation(npm("css-loader"))
    implementation(npm("url-loader"))

    testImplementation(project(":sdk"))
    testImplementation(project(":test-logging"))

    testImplementation(kotlin("test-js"))
    testImplementation(npm("axios-cookiejar-support", "^0.5.0"))
    testImplementation(npm("tough-cookie", "^3.0.1"))
    testImplementation(npm("uuid", "^3.3.2"))
    testImplementation("io.github.microutils:kotlin-logging-js:1.8.3")
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
    testImplementation("com.zegreatrob.testmints:async:+")

}

tasks {
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class)

    val nodeRun by wdioRun(
        pathToNodeApp = "${project(":server").buildDir.absolutePath}/executable/app.js",
        wdioConfig = project.projectDir.resolve("wdio.conf.js"),
        webpackConfig = project.projectDir.resolve("webpack.config.js"),
        webpackedWdioConfigOutput = "config"
    ) {
        dependsOn(compileTestKotlinJs)
        inputs.files(compileTestKotlinJs.outputs.files)
        dependsOn(":server:assemble")
        mustRunAfter(":client:check")
        environment("PORT" to "3099")
    }

    val check by getting {
        dependsOn(nodeRun)
    }
}

fun TaskContainerScope.wdioRun(
    pathToNodeApp: String,
    wdioConfig: File,
    webpackConfig: File,
    webpackedWdioConfigOutput: String,
    block: NodeJsExec.() -> Unit
) = getting(NodeJsExec::class) {
    inputs.files(file(pathToNodeApp).parent)
    inputs.files(wdioConfig)
    outputs.dir("${project.buildDir}/reports/e2e")
    environment(
        "APP_PATH" to pathToNodeApp,
        "NODE_PATH" to "${rootProject.buildDir.path}/js/node_modules",
        "BUILD_DIR" to project.buildDir.absolutePath,
        "WEBPACK_CONFIG" to webpackConfig,
        "WEBPACKED_WDIO_CONFIG_OUTPUT" to webpackedWdioConfigOutput
    )
    val logFile = file("build/logs/run.log")
    logFile.parentFile.mkdirs()
    standardOutput = logFile.outputStream()

    block()
}