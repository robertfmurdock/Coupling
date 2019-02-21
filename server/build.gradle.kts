import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import com.zegreatrob.coupling.build.forEachJsTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("kotlin2js")
    id("com.github.node-gradle.node")
    id("kotlinx-serialization") version "1.3.21"
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
    implementation(kotlin("stdlib"))
    implementation(project(":engine"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")
    implementation("com.soywiz:klock:1.1.1")
    implementation("io.github.microutils:kotlin-logging-js:1.6.25")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.10.0")

    testImplementation(kotlin("test-js"))
    testImplementation(project(":test-style"))
    testImplementation(project(":test-style-async-js"))
    testImplementation(project(":test-logging"))
}

tasks {
    val yarn by getting {
        inputs.file(file("package.json"))
        outputs.dir(file("node_modules"))
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
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val copyServerIcons by creating(Copy::class) {
        from("public")
        into("build/executable/public")
    }

    val copyServerViews by creating(Copy::class) {
        from("views")
        into("build/executable/views")
    }

    val copyServerResources by creating {
        dependsOn(copyServerIcons, copyServerViews)
    }

    val copyClient by creating(Copy::class) {
        dependsOn(":client:compile", copyServerResources)
        from("../client/build/lib")
        into("build/executable/public/app/build")
    }

    val unpackJsGradleDependencies by creating(UnpackGradleDependenciesTask::class) {
        inputs.files(compileKotlin2Js.inputs.files)
        dependsOn(":engine:assemble", ":test-logging:assemble")

        forEachJsTarget(project).let { (main, test) ->
            customCompileConfiguration = main
            customTestCompileConfiguration = test
        }
    }

    val serverCompile by creating(YarnTask::class) {
        dependsOn(yarn, copyServerResources, unpackJsGradleDependencies, compileKotlin2Js)
        mustRunAfter(clean)
        inputs.dir("build/classes")
        inputs.dir("build/node_modules_imported")
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("tsconfig.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("config")
        inputs.dir("lib")
        inputs.dir("public")
        inputs.dir("routes")
        inputs.dir("views")
        inputs.file("app.ts")
        inputs.file("routes.ts")
        inputs.dir("../common")
        outputs.dir(file("build/executable"))
        setEnvironment(mapOf("NODE_ENV" to "production"))
        args = listOf("webpack", "--config", "webpack.config.js")
    }

    val assemble by getting {
        dependsOn(serverCompile, copyClient)
    }

    val serverTest by creating(YarnTask::class) {
        dependsOn(
                yarn,
                unpackJsGradleDependencies,
                compileKotlin2Js,
                compileTestKotlin2Js,
                copyClient
        )
        inputs.file(file("package.json"))
        inputs.files(serverCompile.inputs.files)
        inputs.dir("test/unit")
        outputs.dir("build/test-results/server.unit")

        setEnvironment(mapOf("NODE_PATH" to "build/node_modules_imported"))
        args = listOf("run", "serverTest", "--silent")
    }

    val endpointTest by creating(YarnTask::class) {
        dependsOn(yarn, assemble)
        mustRunAfter(serverTest)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("test/endpoint")
        outputs.dir("../test-output/endpoint")

        setEnvironment(mapOf("NODE_PATH" to "build/node_modules_imported"))
        args = listOf("run", "endpointTest", "--silent")
    }

    val updateWebdriver by creating(YarnTask::class) {
        dependsOn(yarn)
        inputs.file("package.json")
        outputs.dir("node_modules/webdriver-manager/selenium/")
        args = listOf("run", "update-webdriver", "--silent")
    }

    val endToEndTest by creating(YarnTask::class) {
        dependsOn(assemble, updateWebdriver)
        mustRunAfter(serverTest, ":client:test", endpointTest)
        inputs.files(findByPath(":client:test")?.inputs?.files)
        inputs.files(findByPath(":client:compile")?.outputs?.files)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("test/e2e")
        outputs.dir("../test-output/e2e")

        setEnvironment(mapOf("NODE_PATH" to "build/node_modules_imported"))
        args = listOf("run", "protractor", "--silent", "--seleniumAddress", System.getenv("SELENIUM_ADDRESS") ?: "")
    }

    val test by getting {
        dependsOn(serverTest)
    }

    val check by getting {
        dependsOn(endpointTest)
    }

    val start by creating(YarnTask::class) {
        dependsOn(assemble)
        args = listOf("run", "start-built-app")
    }

    val testWatch by creating(NodeTask::class) {
        setArgs(listOf("test/continuous-run.js"))
    }

}