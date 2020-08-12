import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeBinDir
import com.zegreatrob.coupling.build.nodeExecPath
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("kotlinx-serialization") version "1.3.72"
}

kotlin {
    target {
        nodejs()
        useCommonJs()
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
    implementation("com.soywiz.korlibs.klock:klock:1.10.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")
    implementation("com.benasher44:uuid:0.1.0")

    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

}

tasks {
    val clean by getting {
        doLast {
            delete(file("build"))
        }
    }

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
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

    val serverCompile by creating(Exec::class) {
        dependsOn(copyServerResources, compileKotlinJs)
        mustRunAfter(clean)
        inputs.file(compileKotlinJs.outputFile)
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("src/main/javascript")
        inputs.dir("public")
        inputs.dir("views")
        outputs.dir(file("build/executable"))

        environment(
            "NODE_ENV" to "production",
            "NODE_PATH" to nodeModulesDir,
            "PATH" to "$nodeBinDir"
        )


        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")

        commandLine = listOf("$nodeModulesDir/.bin/webpack", "--config", project.projectDir.resolve("webpack.config.js").absolutePath)
    }

    val assemble by getting {
        dependsOn(serverCompile, copyClient)
    }

    val updateDependencies by creating(Exec::class) {
        val packageJson: String? by rootProject
        environment("NODE_PATH" to nodeModulesDir)
        commandLine = listOf(
            nodeExecPath,
            "$nodeModulesDir/.bin/ncu",
            "-u",
            "--packageFile",
            "${System.getenv("PWD")}/$packageJson"
        )
    }

    val start by creating(Exec::class) {
        dependsOn(assemble)
        environment(
            "NODE_ENV" to "production",
            "NODE_PATH" to nodeModulesDir
        )
        commandLine = listOf(nodeExecPath, project.relativePath("startup"))
    }

}

