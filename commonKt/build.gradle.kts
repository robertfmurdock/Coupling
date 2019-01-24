
import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.20"
    id("com.github.node-gradle.node")
}

node {
    version = "11.6.0"
    npmVersion = "6.5.0"
    yarnVersion = "1.13.0"
    download = true
}

apply {
    plugin("kotlin-dce-js")
}

repositories {
    mavenCentral()
}

kotlin {

    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.3.20")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                api(project(":test-style"))
            }
        }
    }
}

tasks {

    getByName<Kotlin2JsCompile>("compileKotlinJs") {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    getByName<Kotlin2JsCompile>("compileTestKotlinJs") {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    getByName<KotlinJsDce>("runDceJsKotlin") {
        keep("commonKt.pairingTimeCalculator", "commonKt.historyFromArray")
    }

    getByName("assemble") {
        dependsOn("runDceJsKotlin")
    }

    getByName("jsTest") {
        dependsOn("jasmine")
    }

    task<NodeTask>("jasmine") {
        dependsOn("yarn", "compileTestKotlinJs")

        val compileTask = getByName<Kotlin2JsCompile>("compileKotlinJs")
        val compileTestTask = getByName<Kotlin2JsCompile>("compileTestKotlinJs")

        setEnvironment(mapOf("NODE_PATH" to listOf(
                "node_modules",
                compileTask.outputFile.parent,
                (getByPath(":test-style:compileKotlinJs") as Kotlin2JsCompile).outputFile.parent
        ).joinToString(":")))
        setScript(file("test-run.js"))
        setArgs(listOf("${compileTestTask.outputFile}"))
    }

}


