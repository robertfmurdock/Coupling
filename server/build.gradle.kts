import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeBinDir
import com.zegreatrob.coupling.build.nodeExec
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("kotlinx-serialization") version "1.4.10"
}

kotlin {
    js {
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.0")
    implementation("com.soywiz.korlibs.klock:klock:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0")
    implementation("com.benasher44:uuid:0.2.2")

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

    val compileKotlinJs by getting(Kotlin2JsCompile::class)

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
        inputs.dir("public")
        outputs.dir(file("build/executable"))

        environment(
            "NODE_ENV" to "production",
            "NODE_PATH" to nodeModulesDir,
            "PATH" to "$nodeBinDir"
        )

        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")

        commandLine = listOf(
            "$nodeModulesDir/.bin/webpack",
            "--config",
            project.projectDir.resolve("webpack.config.js").absolutePath
        )
    }

    val assemble by getting {
        dependsOn(serverCompile, copyClient)
    }

    val packageJson: String? by rootProject

    create<Exec>("updateDependencies") {
        nodeExec(
            compileKotlinJs,
            listOf("$nodeModulesDir/.bin/ncu", "-u", "--packageFile", "${System.getenv("PWD")}/$packageJson")
        )
    }

    create<Exec>("start") {
        nodeExec(compileKotlinJs, listOf(project.relativePath("startup")))
        dependsOn(assemble)
        environment("NODE_ENV", "production")
    }

}
