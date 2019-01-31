import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.frontend.npm.UnpackGradleDependenciesTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJsDce

plugins {
    id("org.jetbrains.kotlin.multiplatform")
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
    jcenter()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
}

kotlin {

    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.3.20")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.1.0")
                implementation("com.soywiz:klock:1.1.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation(project(":test-style"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.3.20")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-io-js:0.1.4")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
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
        keep(
                "commonKt.pairingTimeCalculator",
                "commonKt.historyFromArray",
                "commonKt.ComposeStatisticsActionDispatcher",
                "commonKt.performComposeStatisticsAction"
        )
    }

    val unpackJsGradleDependencies by creating(UnpackGradleDependenciesTask::class) {
        dependsOn(":test-style:build")

        customCompileConfiguration = listOf(
                project.configurations.getByName("jsCompile"),
                project.configurations.getByName("jsMainImplementation"),
                project.configurations.getByName("jsMainRuntimeOnly"),
                project.configurations.getByName("jsMainApi"),
                project.configurations.getByName("jsDefault")
        )
        customTestCompileConfiguration = listOf(
                project.configurations.getByName("jsTestApiDependenciesMetadata"),
                project.configurations.getByName("jsTestCompile"),
                project.configurations.getByName("jsTestCompileClasspath"),
                project.configurations.getByName("jsTestCompileOnly"),
                project.configurations.getByName("jsTestCompileOnlyDependenciesMetadata"),
                project.configurations.getByName("jsTestImplementationDependenciesMetadata"),
                project.configurations.getByName("jsTestRuntime"),
                project.configurations.getByName("jsTestRuntimeClasspath"),
                project.configurations.getByName("jsTestRuntimeOnlyDependenciesMetadata"),
                project.configurations.getByName("jsTestCompile"),
                project.configurations.getByName("jsTestCompileOnly")
        )
    }

    val jsTestProcessResources by getting(ProcessResources::class)

    getByName("assemble") {
        dependsOn(runDceJsKotlin, unpackJsGradleDependencies)
    }

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

        relevantPaths.forEach { inputs.dir(it) }

        setEnvironment(mapOf("NODE_PATH" to relevantPaths.joinToString(":")))

        setArgs(listOf("${compileTestKotlinJs.outputFile}"))

        outputs.dir("build/test-results/jsTest")
    }

    getByName("jsTest") {
        dependsOn(jasmine)
    }

}


