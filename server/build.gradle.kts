import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeBinDir
import com.zegreatrob.coupling.build.nodeExec
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("kotlinx-serialization") version "1.5.0"
}

kotlin {
    js {
        nodejs()
        useCommonJs()
        binaries.executable()
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }
    }
}

val packageJson = loadPackageJson()


val appConfiguration: Configuration by configurations.creating {
    extendsFrom(configurations["implementation"])
}

val clientConfiguration: Configuration by configurations.creating

inline fun <reified T : Named> Project.namedAttribute(value: String) = objects.named(T::class.java, value)

dependencies {
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    implementation(kotlin("stdlib"))
    implementation(project(":json"))
    implementation(project(":repository:dynamo"))
    implementation(project(":repository:memory"))
    implementation(project("server_action"))
    implementation("com.zegreatrob.testmints:minjson:4.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("com.soywiz.korlibs.klock:klock:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
    implementation("com.benasher44:uuid:0.3.0")

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

    val processResources by getting(ProcessResources::class) {}

    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}

    val serverCompile by creating(Exec::class) {
        dependsOn(copyServerResources, compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)
        inputs.dir(compileKotlinJs.outputFile)
        inputs.dir(processResources.destinationDir.path)
        inputs.file(compileProductionExecutableKotlinJs.outputFile)
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
        dependsOn(serverCompile)
    }

    val packageJson: String? by rootProject

    create<Exec>("updateDependencies") {
        nodeExec(
            compileKotlinJs,
            listOf("$nodeModulesDir/.bin/ncu", "-u", "--packageFile", "${System.getenv("PWD")}/$packageJson")
        )
    }

    create<Exec>("start") {
        dependsOn(assemble, clientConfiguration)
        nodeExec(compileKotlinJs, listOf(project.relativePath("startup")))
        environment("NODE_ENV", "production")
        environment(
            "CLIENT_PATH",
            System.getenv("CLIENT_PATH")
                ?.let { if (it.isEmpty()) null else it }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    artifacts {
        add(appConfiguration.name, compileKotlinJs.outputFile) {
            builtBy(compileKotlinJs)
        }
        add(appConfiguration.name, file("build/executable")) {
            builtBy(serverCompile)
        }
    }
}
