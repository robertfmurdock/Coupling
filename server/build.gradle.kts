import com.zegreatrob.coupling.plugins.NodeExec
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.node")
    id("com.zegreatrob.coupling.plugins.serialization")
    id("com.bmuschko.docker-remote-api")
}

kotlin.js().nodejs()
kotlin.sourceSets {
    getByName("main") {
        resources.srcDir("src/main/javascript")
    }
    all { languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi") }
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
    implementation("com.zegreatrob.testmints:minjson")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("com.soywiz.korlibs.klock:klock:2.4.10")
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

    val serverCompile by creating(NodeExec::class) {
        dependsOn(compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)
        inputs.dir(compileKotlinJs.outputFileProperty)
        inputs.dir(processResources.destinationDir.path)
        inputs.file(compileProductionExecutableKotlinJs.outputFileProperty)
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("public")
        outputs.dir(file("build/webpack-output"))

        nodeCommand = "webpack"
        arguments = listOf("--config", project.projectDir.resolve("webpack.config.js").absolutePath)
        environment("NODE_ENV" to "production")
        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")
    }

    create<NodeExec>("serverStats") {
        dependsOn(compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)

        nodeCommand = "webpack"
        arguments = listOf(
            "--config",
            project.projectDir.resolve("webpack.config.js").absolutePath,
            "--profile",
            "--json=${rootDir.absolutePath}/compilation-stats.json"
        )
        environment("NODE_ENV" to "production")
        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")
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

    create<NodeExec>("updateDependencies") {
        dependsOn(test, compileKotlinJs)
        nodeCommand = "ncu"
        arguments = listOf("-u", "--packageFile", "${System.getenv("PWD")}/$packageJson")
    }

    create<NodeExec>("start") {
        dependsOn(assemble, clientConfiguration, compileKotlinJs)
        arguments = listOf(project.relativePath("startup"))
        environment("NODE_ENV", "production")
        environment(
            "CLIENT_PATH",
            System.getenv("CLIENT_PATH")
                ?.let { it.ifEmpty { null } }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    val buildImage by creating(com.bmuschko.gradle.docker.tasks.image.DockerBuildImage::class) {
        dependsOn(assemble, compileKotlinJs)
        inputDir.set(project.projectDir)
        images.add("zegreatrob/coupling-serverless:latest")
    }

    create<NodeExec>("serverlessStart") {
        dependsOn(assemble, clientConfiguration, test, compileKotlinJs)
        val serverlessConfigFile = project.relativePath("serverless.yml")
        nodeCommand = "serverless"
        arguments = listOf("offline", "--config", serverlessConfigFile, "start")
        environment("NODE_ENV", "production")
        environment(
            "LAMBDA_ENDPOINT" to "https://localhost",
            "WEBSOCKET_HOST" to "socket.localhost",
            "NODE_TLS_REJECT_UNAUTHORIZED" to 0
        )
        environment(
            "CLIENT_PATH",
            System.getenv("CLIENT_PATH")
                ?.let { it.ifEmpty { null } }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    val serverlessBuildDir = "${buildDir.absolutePath}/lambda-dist"

    val serverlessBuild by creating(NodeExec::class) {
        dependsOn(assemble, test, compileKotlinJs)
        environment(
            "AWS_ACCESS_KEY_ID" to (System.getenv("AWS_ACCESS_KEY_ID") ?: "fake"),
            "AWS_SECRET_ACCESS_KEY" to (System.getenv("AWS_SECRET_ACCESS_KEY") ?: "fake"),
            "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/${version}",
        )
        nodeCommand = "serverless"
        arguments = listOf(
            "package",
            "--config",
            project.relativePath("serverless.yml"),
            "--package",
            serverlessBuildDir,
            "--stage",
            serverlessStage
        )
    }

    val serverlessDeploy by creating(NodeExec::class) {
        dependsOn(serverlessBuild, compileKotlinJs)
        mustRunAfter(
            ":release",
            ":updateGithubRelease",
            ":client:uploadToS3",
        )
        nodeCommand = "serverless"
        arguments = listOf(
            "deploy",
            "--config",
            project.relativePath("serverless.yml"),
            "--package",
            serverlessBuildDir,
            "--stage",
            serverlessStage
        )
    }
    findByPath(":release")!!.finalizedBy(serverlessDeploy)

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
