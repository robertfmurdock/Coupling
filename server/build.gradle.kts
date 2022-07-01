import com.zegreatrob.coupling.plugins.NodeExec

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
    clientConfiguration(
        project(mapOf("path" to ":client", "configuration" to "clientConfiguration"))
    )
    implementation(kotlin("stdlib"))
    implementation(project(":coupling-libraries:json"))
    implementation(project(":coupling-libraries:server_action"))
    implementation(project(":coupling-libraries:repository-memory"))
    implementation(project(":dynamo"))
    implementation("com.zegreatrob.jsmints:minjson")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("com.soywiz.korlibs.klock:klock:2.7.0")
    implementation("com.benasher44:uuid:0.4.1")
}

tasks {
    clean {
        doLast {
            delete(file("build"))
        }
    }

    val serverCompile by registering(NodeExec::class) {
        dependsOn(compileKotlinJs, processResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)
        inputs.dir(compileKotlinJs.get().outputFileProperty)
        inputs.dir(processResources.get().destinationDir.path)
        inputs.file(compileProductionExecutableKotlinJs.get().outputFileProperty)
        inputs.file(file("package.json"))
        inputs.file(file("webpack.config.js"))
        inputs.dir("public")
        outputs.dir(file("build/webpack-output"))
        outputs.cacheIf { true }
        compilationName = "main"
        nodeCommand = "webpack"
        arguments = listOf("--config", project.projectDir.resolve("webpack.config.js").absolutePath)
        environment("NODE_ENV" to "production")
        workingDir = file("${rootProject.buildDir.resolve("js").resolve("packages/Coupling-server")}")
    }

    register<NodeExec>("serverStats") {
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

    val copyServerIcons by registering(Copy::class) {
        from("public")
        into("build/executable/public")
    }

    val copyServerViews by registering(Copy::class) {
        from("views")
        into("build/executable/views")
    }

    val copyServerExecutable by registering(Copy::class) {
        dependsOn(serverCompile)
        from("build/webpack-output")
        into("build/executable")
    }

    val copyServerResources by registering {
        dependsOn(copyServerIcons, copyServerViews)
    }

    val serverAssemble by registering {
        dependsOn(copyServerResources, copyServerExecutable)
    }

    assemble {
        dependsOn(serverAssemble)
    }

    register<NodeExec>("start") {
        dependsOn(assemble, clientConfiguration, compileKotlinJs)
        arguments = listOf(project.relativePath("startup"))
        environment("NODE_ENV", "production")
    }

    val prepareDockerData by registering(Copy::class) {
        dependsOn(assemble, compileKotlinJs)
        from("build") {
            include("executable/**")
        }
        from(project.projectDir) {
            include("Dockerfile", "serverless.yml", "deploy/**")
        }

        destinationDir = file("build/docker-data")
    }

    val buildImage by registering(com.bmuschko.gradle.docker.tasks.image.DockerBuildImage::class) {
        dependsOn(prepareDockerData, ":server-base:pullImage")
        inputDir.set(file("build/docker-data"))
        images.add("ghcr.io/robertfmurdock/coupling-serverless:latest")
    }

    register<NodeExec>("serverlessStart") {
        dependsOn(assemble, clientConfiguration, test, compileKotlinJs)
        val serverlessConfigFile = project.relativePath("serverless.yml")
        nodeCommand = "serverless"
        arguments = listOf("offline", "--config", serverlessConfigFile, "start")
        environment("NODE_ENV", "production")
        environment(
            "LAMBDA_ENDPOINT" to "http://localhost:3002",
            "WEBSOCKET_HOST" to "localhost:3001",
            "NODE_TLS_REJECT_UNAUTHORIZED" to 0
        )
    }

    fun NodeExec.configureBuild(stage: String) {
        val serverlessBuildDir = "${project.buildDir.absolutePath}/$stage/lambda-dist"
        dependsOn(assemble, test, compileKotlinJs, ":calculateVersion")
        environment(
            "AWS_ACCESS_KEY_ID" to (System.getenv("AWS_ACCESS_KEY_ID") ?: "fake"),
            "AWS_SECRET_ACCESS_KEY" to (System.getenv("AWS_SECRET_ACCESS_KEY") ?: "fake"),
            "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/${project.version}",
        )
        nodeCommand = "serverless"
        arguments = listOf(
            "package",
            "--config",
            project.relativePath("serverless.yml"),
            "--package",
            serverlessBuildDir,
            "--stage",
            stage
        )
    }
    register("serverlessBuild", NodeExec::class) {
        configureBuild("prod")
    }
    register("serverlessBuildSandbox", NodeExec::class) {
        configureBuild("sandbox")
    }
    register("serverlessBuildPrerelease", NodeExec::class) {
        configureBuild("prerelease")
    }
    artifacts {
        add(appConfiguration.name, file("build/executable")) {
            builtBy(serverAssemble)
        }
    }
}

artifacts {
    add(appConfiguration.name, tasks.compileKotlinJs.get().outputFileProperty) {
        builtBy(tasks.compileKotlinJs)
    }
}
