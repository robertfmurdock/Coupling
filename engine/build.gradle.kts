
import com.moowork.gradle.node.task.NodeTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import com.zegreatrob.coupling.build.forEachJsTarget
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.github.node-gradle.node")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

apply {
    plugin("kotlin-dce-js")
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":test-style"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("io.kotlintest:kotlintest-runner-junit5:3.1.11")
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
    val runDceJsKotlin by getting(KotlinJsDce::class) {
        keep("engine.spinContext")
    }

    val unpackJsGradleDependencies by creating(UnpackGradleDependenciesTask::class) {
        dependsOn(":test-style:assemble", ":commonKt:assemble")

        forEachJsTarget(project).let { (main, test) ->
            customCompileConfiguration = main
            customTestCompileConfiguration = test
        }
    }
    val jsTestProcessResources by getting(ProcessResources::class)

    val assemble by getting
    assemble.dependsOn(runDceJsKotlin, unpackJsGradleDependencies)

    val jasmine by creating(NodeTask::class) {
        dependsOn("yarn", compileKotlinJs, compileTestKotlinJs, unpackJsGradleDependencies)
        mustRunAfter(compileTestKotlinJs, jsTestProcessResources)

        val relevantPaths = listOf(
                "node_modules",
                "build/node_modules_imported",
                compileKotlinJs.outputFile.parent,
                jsTestProcessResources.destinationDir
        )

        inputs.file(compileTestKotlinJs.outputFile)

        val script = file("test-run.js")

        inputs.file(script)
        setScript(script)

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
