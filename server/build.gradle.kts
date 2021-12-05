
import com.zegreatrob.coupling.build.nodeBinDir
import com.zegreatrob.coupling.build.nodeExec
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.bmuschko.docker-remote-api")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("kotlinx-serialization") version "1.6.0"
}

kotlin {
    js {
        nodejs()
        useCommonJs()
        binaries.executable()
    }
    sourceSets {
        getByName("main") {
            resources.srcDir("src/main/javascript")
        }
    }
}

val appConfiguration: Configuration by configurations.creating {
    extendsFrom(configurations["implementation"])
}

val clientConfiguration: Configuration by configurations.creating

inline fun <reified T : Named> Project.namedAttribute(value: String) = objects.named(T::class.java, value)

dependencies {
    clientConfiguration(project(mapOf("path" to ":client", "configuration" to "clientConfiguration")))
    implementation(kotlin("stdlib"))
    implementation(project(":json"))
    implementation(project(":repository-dynamo"))
    implementation(project(":repository-memory"))
    implementation(project(":server_action"))
    implementation("com.zegreatrob.testmints:minjson:5.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("com.soywiz.korlibs.klock:klock:2.4.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("com.benasher44:uuid:0.3.1")
}

tasks {
    val clean by getting {
        doLast {
            delete(file("build"))
        }
    }

    val compileKotlinJs by getting(Kotlin2JsCompile::class)

    val processResources by getting(ProcessResources::class) {}

    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}

    val serverCompile by creating(Exec::class) {
        dependsOn(compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)
        inputs.dir(compileKotlinJs.outputFileProperty)
        inputs.dir(processResources.destinationDir.path)
        inputs.file(compileProductionExecutableKotlinJs.outputFileProperty)
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("public")
        outputs.dir(file("build/webpack-output"))

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

    create<Exec>("serverStats") {
        dependsOn(compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)

        environment(
            "NODE_ENV" to "production",
            "NODE_PATH" to nodeModulesDir,
            "PATH" to "$nodeBinDir"
        )

        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")

        commandLine = listOf(
            "$nodeModulesDir/.bin/webpack",
            "--config",
            project.projectDir.resolve("webpack.config.js").absolutePath,
            "--profile",
            "--json=${rootDir.absolutePath}/compilation-stats.json"
        )
    }

    val copyServerIcons by creating(Copy::class) {
        from("public")
        into("build/executable/public")
    }

    val copyServerViews by creating(Copy::class) {
        from("views")
        into("build/executable/views")
    }

    val copyServerExecutable by creating(Copy::class) {
        dependsOn(serverCompile)
        from("build/webpack-output")
        into("build/executable")
    }

    val copyServerResources by creating {
        dependsOn(copyServerIcons, copyServerViews)
    }

    val serverAssemble by creating {
        dependsOn(copyServerResources, copyServerExecutable)
    }

    val assemble by getting {
        dependsOn(serverAssemble)
    }

    val packageJson: String? by rootProject

    val test by getting

    create<Exec>("updateDependencies") {
        dependsOn(test)
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
                ?.let { it.ifEmpty { null } }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    create<Exec>("serverlessStart") {
        dependsOn(assemble, clientConfiguration, test)
        val serverlessConfigFile = project.relativePath("serverless.yml")
        nodeExec(
            compileKotlinJs,
            listOf("$nodeModulesDir/.bin/serverless", "offline", "--config", serverlessConfigFile, "start")
        )
        environment("NODE_ENV", "production")
        environment(
            "LAMBDA_ENDPOINT" to "http://localhost:3002",
            "WEBSOCKET_HOST" to "localhost:3001"
        )
        environment(
            "CLIENT_PATH",
            System.getenv("CLIENT_PATH")
                ?.let { it.ifEmpty { null } }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    val serverlessBuildDir = "${buildDir.absolutePath}/lambda-dist"

    val serverlessBuild by creating(Exec::class) {
        dependsOn(assemble, test)
        environment(
            "AWS_ACCESS_KEY_ID" to (System.getenv("AWS_ACCESS_KEY_ID") ?: "fake"),
            "AWS_SECRET_ACCESS_KEY" to (System.getenv("AWS_SECRET_ACCESS_KEY") ?: "fake"),
            "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/${version}",
        )
        nodeExec(
            compileKotlinJs,
            listOf(
                "$nodeModulesDir/.bin/serverless",
                "package",
                "--config",
                project.relativePath("serverless.yml"),
                "--package",
                serverlessBuildDir,
                "--stage",
                serverlessStage
            )
        )
    }

    create<Exec>("serverlessDeploy") {
        dependsOn(serverlessBuild)
        mustRunAfter(
            ":release",
            ":updateGithubRelease",
            ":client:uploadToS3",
        )
        nodeExec(
            compileKotlinJs,
            listOf(
                "$nodeModulesDir/.bin/serverless",
                "deploy",
                "--config",
                project.relativePath("serverless.yml"),
                "--package",
                serverlessBuildDir,
                "--stage",
                serverlessStage
            )
        )
    }


    artifacts {
        add(appConfiguration.name, compileKotlinJs.outputFileProperty) {
            builtBy(compileKotlinJs)
        }
        add(appConfiguration.name, file("build/executable")) {
            builtBy(serverAssemble)
        }
    }
}

val serverlessStage
    get() = if ("$version".contains("SNAPSHOT")) {
        "dev"
    } else {
        "prod"
    }
