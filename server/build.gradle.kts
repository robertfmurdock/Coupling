import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}

kotlin.js().nodejs()

kotlin.sourceSets {
    getByName("jsMain") {
        resources.srcDir("src/jsMain/javascript")
    }
}

val appConfiguration: Configuration by configurations.creating {
    extendsFrom(configurations["jsMainImplementation"])
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "server")
    }
}

val clientConfiguration: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Attribute.of("com.zegreatrob.executable", String::class.java), "client")
    }
}

inline fun <reified T : Named> Project.namedAttribute(value: String) = objects.named(T::class.java, value)

dependencies {
    clientConfiguration(project(":client"))
    jsMainImplementation(project("action"))
    jsMainImplementation(project("discord"))
    jsMainImplementation(project("secret"))
    jsMainImplementation(project("slack"))
    jsMainImplementation(project(":libraries:json"))
    jsMainImplementation(project(":libraries:repository:dynamo"))
    jsMainImplementation(project(":libraries:repository:memory"))
    jsMainImplementation("com.benasher44:uuid")
    jsMainImplementation("com.zegreatrob.jsmints:minjson")
    jsMainImplementation("io.github.oshai:kotlin-logging")
    jsMainImplementation("io.ktor:ktor-client-logging")
    jsMainImplementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    jsMainImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    jsMainImplementation(npmConstrained("@aws-sdk/client-apigatewaymanagementapi"))
    jsMainImplementation(npmConstrained("@aws-sdk/client-lambda"))
    jsMainImplementation(npmConstrained("@graphql-tools/schema"))
    jsMainImplementation(npmConstrained("@graphql-tools/stitch"))
    jsMainImplementation(npmConstrained("body-parser"))
    jsMainImplementation(npmConstrained("compression"))
    jsMainImplementation(npmConstrained("cookie-parser"))
    jsMainImplementation(npmConstrained("express"))
    jsMainImplementation(npmConstrained("express-jwt"))
    jsMainImplementation(npmConstrained("express-statsd"))
    jsMainImplementation(npmConstrained("fs-extra"))
    jsMainImplementation(npmConstrained("graphql"))
    jsMainImplementation(npmConstrained("jose"))
    jsMainImplementation(npmConstrained("jwks-rsa"))
    jsMainImplementation(npmConstrained("method-override"))
    jsMainImplementation(npmConstrained("mime"))
    jsMainImplementation(npmConstrained("minimist"))
    jsMainImplementation(npmConstrained("on-finished"))
    jsMainImplementation(npmConstrained("parse5"))
    jsMainImplementation(npmConstrained("stripe"))

    jsTestImplementation(project(":libraries:stub-model"))
    jsTestImplementation("com.zegreatrob.testmints:async")
    jsTestImplementation("com.zegreatrob.testmints:minassert")
    jsTestImplementation("com.zegreatrob.testmints:standard")
    jsTestImplementation(npmConstrained("serverless"))
    jsTestImplementation(npmConstrained("serverless-offline"))
    jsTestImplementation(npmConstrained("serverless-offline-ssm"))
}

tasks {
    val cleanBuild by registering(Delete::class) {
        setDelete(file("build"))
    }
    clean {
        dependsOn(cleanBuild)
    }

    val serverCompile by registering(NodeExec::class) {
        dependsOn(
            "jsPackageJson",
            ":kotlinNpmInstall",
            compileKotlinJs,
            jsProcessResources,
            compileProductionExecutableKotlinJs,
            "jsProductionExecutableCompileSync",
        )
        mustRunAfter(clean)
        inputs.dir(jsProcessResources.map { it.destinationDir.path })
        inputs.file(compileProductionExecutableKotlinJs.map {
            it.destinationDirectory.file(it.compilerOptions.moduleName.map { "$it.js" })
        })
        inputs.file(file("webpack.config.js"))
        inputs.dir("public")
        outputs.dir(file("build/webpack-output"))
        outputs.cacheIf { true }
        val compilationName = "main"
        val compilation = kotlin.js().compilations.named(compilationName).get()

        inputs.file(compilation.npmProject.packageJsonFile)

        setup(project)
        nodeModulesDir = compilation?.npmProject?.nodeModulesDir
        npmProjectDir = compilation?.npmProject?.dir

        nodeCommand = "webpack"
        arguments = listOf("--config", project.projectDir.resolve("webpack.config.js").absolutePath)
        environment("NODE_ENV" to "production")
        workingDir = rootProject.layout.buildDirectory.file("js/packages/Coupling-server").get().asFile
    }

    register<NodeExec>("serverStats") {
        setup(project)
        dependsOn(compileKotlinJs, jsProcessResources, compileProductionExecutableKotlinJs)
        mustRunAfter(clean)

        nodeCommand = "webpack"
        arguments = listOf(
            "--config",
            project.projectDir.resolve("webpack.config.js").absolutePath,
            "--profile",
            "--json=${rootDir.absolutePath}/compilation-stats.json"
        )
        environment("NODE_ENV" to "production")
        workingDir = rootProject.layout.buildDirectory.file("js/packages/Coupling-server").get().asFile
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

    register<Exec>("buildImage") {
        dependsOn(
            prepareDockerData,
            ":server:base:pullImage"
        )
        commandLine(
            "docker build --tag ghcr.io/robertfmurdock/coupling-serverless:latest build/docker-data"
                .split(" ")
        )
    }

    register<NodeExec>("serverlessStart") {
        dependsOn(assemble, clientConfiguration, jsTest, compileKotlinJs)
        setup(project)
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
        val serverlessBuildDir = project.layout.buildDirectory.file("$stage/lambda-dist")
        setup(project)
        dependsOn(assemble, jsTest, compileKotlinJs, ":calculateVersion")
        val releaseVersion = rootProject.version
        environment("CLIENT_URL" to "https://assets.zegreatrob.com/coupling/$releaseVersion")
        environment("CLI_URL" to "https://assets.zegreatrob.com/coupling-cli/$releaseVersion")
        enabled = "$releaseVersion".run { !(contains("SNAPSHOT") || isBlank()) }
        nodeCommand = "serverless"
        arguments = listOf(
            "package",
            "--config",
            project.relativePath("serverless.yml"),
            "--package",
            serverlessBuildDir.get().asFile.absolutePath,
            "--stage",
            stage
        )
    }
    register<NodeExec>("serverlessBuild") {
        configureBuild("prod")
    }
    register<NodeExec>("serverlessBuildSandbox") {
        configureBuild("sandbox")
    }
    register<NodeExec>("serverlessBuildPrerelease") {
        configureBuild("prerelease")
    }
}

artifacts {
    add(appConfiguration.name, tasks.compileKotlinJs.map {
        it.destinationDirectory.file(it.compilerOptions.moduleName.map { "$it.js" })
    }) {
        builtBy(tasks.compileKotlinJs)
    }
    add(appConfiguration.name, file("build/executable")) {
        builtBy("serverAssemble")
    }
}
