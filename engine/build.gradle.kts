
import com.moowork.gradle.node.task.NodeTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.github.node-gradle.node")
    id("smol-js")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":commonKt"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")
                implementation("com.soywiz:klock:1.1.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":test-style"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsTest by getting {
            dependencies {
                api(project(":logging"))
            }
        }
    }
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }

    val unpackJsGradleDependencies by getting(UnpackGradleDependenciesTask::class) {
        dependsOn(":test-style:assemble", ":commonKt:assemble")
    }

    val jsTestProcessResources by getting(ProcessResources::class)

    val assemble by getting
    assemble.dependsOn(unpackJsGradleDependencies)

    val jasmine by getting(NodeTask::class) {
        val relevantPaths = listOf(
                "node_modules",
                "build/node_modules_imported",
                compileKotlinJs.outputFile.parent,
                jsTestProcessResources.destinationDir
        )

        inputs.file(compileTestKotlinJs.outputFile)

        relevantPaths.filter { file(it).exists() }.forEach { inputs.dir(it) }

        setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

        setArgs(listOf("${compileTestKotlinJs.outputFile}"))

        outputs.dir("build/test-results/jsTest")
    }

    val jsTest by getting
    jsTest.dependsOn(jasmine)

    val test by creating {
        dependsOn(jsTest)
    }
}
