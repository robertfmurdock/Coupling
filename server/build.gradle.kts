import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("com.github.node-gradle.node")
    id("kotlinx-serialization") version "1.3.72"
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

kotlin {
    target {
        nodejs {
        }
        compilations {
            val endToEndTest by compilations.creating
        }
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }

        val test by getting {
            dependencies {
                implementation(npm("uuid", "^3.3.2"))
            }
        }

        val endToEndTest by getting {
            dependsOn(test)

            dependencies {
                implementation(project(":sdk"))
                implementation(npm("axios-cookiejar-support", "^0.5.0"))
                implementation(npm("tough-cookie", "^3.0.1"))
                implementation(npm("uuid", "^3.3.2"))
            }
        }
    }
}

val packageJson = loadPackageJson()

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":json"))
    implementation(project(":repository:dynamo"))
    implementation(project(":repository:memory"))
    implementation(project("server_action"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.5")
    implementation("com.soywiz.korlibs.klock:klock:1.10.5")
    implementation("io.github.microutils:kotlin-logging-js:1.7.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")
    implementation("com.benasher44:uuid:0.1.0")

    val includeOnly = listOf(
        "compression",
        "connect-dynamodb",
        "cookie-parser",
        "express",
        "express-session",
        "express-statsd",
        "express-ws",
        "method-override",
        "on-finished",
        "serve-favicon",
        "passport",
        "passport-custom",
        "passport-local",
        "passport-azure-ad",
        "errorhandler",
        "google-auth-library"
    )

    packageJson.dependencies()
        .filter { includeOnly.contains(it.first) }
        .forEach {
            implementation(npm(it.first, it.second.asText()))
        }

    testImplementation(kotlin("test-js"))
    testImplementation(project(":test-logging"))
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
    testImplementation("com.zegreatrob.testmints:async-js:+")

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

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val compileEndToEndTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
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
        dependsOn(":client:assemble", copyServerResources)
        from("../client/build/distributions")
        into("build/executable/public/app/build")
    }

    val serverCompile by creating(YarnTask::class) {
        dependsOn(yarn, copyServerResources, compileKotlinJs)
        mustRunAfter(clean)
        inputs.file(compileKotlinJs.outputFile)
        inputs.dir("node_modules")
        inputs.file(file("package.json"))
        inputs.file(file("tsconfig.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("src/main/javascript")
        inputs.dir("public")
        inputs.dir("views")
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
            compileKotlinJs,
            compileTestKotlinJs,
            copyClient
        )
        inputs.file(file("package.json"))
        inputs.files(serverCompile.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.dir("test/unit")
        outputs.dir("build/test-results/server.unit")

        setEnvironment(mapOf("NODE_PATH" to "${rootProject.buildDir.path}/js/node_modules"))
        args = listOf("run", "serverTest")
    }

    val updateWebdriver by creating(YarnTask::class) {
        dependsOn(yarn)
        inputs.file("package.json")
        outputs.dir("node_modules/webdriver-manager/selenium/")
        args = listOf("run", "update-webdriver", "--silent")
    }

    val endToEndTest by creating(YarnTask::class) {
        dependsOn(assemble, updateWebdriver, compileEndToEndTestKotlinJs)
        mustRunAfter(serverTest, ":client:test", ":sdk:endpointTest")
        inputs.files(findByPath(":client:test")?.inputs?.files)
        inputs.files(findByPath(":client:assemble")?.outputs?.files)
        inputs.files(serverTest.inputs.files)
        inputs.files(serverCompile.outputs.files)
        inputs.files(compileEndToEndTestKotlinJs.outputs.files)
        inputs.file(file("package.json"))
        inputs.dir("test/e2e")
        outputs.dir("${project.buildDir}/test-results/e2e")

        setEnvironment(mapOf("NODE_PATH" to "${rootProject.buildDir.path}/js/node_modules"))
        args = listOf("run", "protractor", "--silent", "--seleniumAddress", System.getenv("SELENIUM_ADDRESS") ?: "")
    }

    val updateDependencies by creating(YarnTask::class) {
        dependsOn(yarn)
        args = listOf("run", "ncu", "-u")
    }

    val test by getting {
        dependsOn(serverTest)
    }

    val start by creating(YarnTask::class) {
        dependsOn(assemble)
        args = listOf("run", "start-built-app")
    }

    val testWatch by creating(NodeTask::class) {
        setArgs(listOf("test/continuous-run.js"))
    }

}