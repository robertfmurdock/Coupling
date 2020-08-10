import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    target {
        nodejs()
        useCommonJs()
        compilations {
            val endToEndTest by compilations.creating
        }
    }
    sourceSets {
        val endToEndTest by getting {
            dependencies {
                implementation(project(":sdk"))
                implementation(project(":test-logging"))
                implementation(kotlin("test-js"))
                implementation(npm("axios-cookiejar-support", "^0.5.0"))
                implementation(npm("tough-cookie", "^3.0.1"))
                implementation(npm("uuid", "^3.3.2"))
                implementation("io.github.microutils:kotlin-logging-js:1.8.3")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.zegreatrob.testmints:async:+")
            }
        }
    }
}

dependencies {
    implementation(project(":test-logging"))
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.110-kotlin-1.3.72")
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
}

tasks {
    val compileEndToEndTestKotlinJs by getting(Kotlin2JsCompile::class) {
    }

    val nodeRun by getting(NodeJsExec::class) {
        dependsOn(compileEndToEndTestKotlinJs)
        dependsOn(":server:assemble")
        mustRunAfter(":client:test", ":sdk:endpointTest")

        inputs.files(findByPath(":client:test")?.inputs?.files)
        inputs.files(findByPath(":client:assemble")?.outputs?.files)
        inputs.files(findByPath(":server:serverCompile")?.outputs?.files)
        inputs.files(compileEndToEndTestKotlinJs.outputs.files)
        outputs.dir("${project.buildDir}/reports/e2e")

        environment("NODE_PATH" to "${rootProject.buildDir.path}/js/node_modules:${project.projectDir.path}")

        val logFile = file("build/logs/run.log")
        logFile.parentFile.mkdirs()
        standardOutput = logFile.outputStream()
    }

    val check by getting {
        dependsOn(nodeRun)
    }
}
