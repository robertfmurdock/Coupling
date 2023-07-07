import com.zegreatrob.coupling.plugins.NodeExec
import com.zegreatrob.coupling.plugins.setup
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    kotlin("plugin.serialization")
}

kotlin.js().nodejs()

kotlin.sourceSets {
    getByName("main") {
        resources.srcDir("src/main/javascript")
    }
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
    implementation(project("action"))
    implementation(project("secret"))
    implementation(project("slack"))
    implementation(project(":libraries:json"))
    implementation(project(":libraries:repository:dynamo"))
    implementation(project(":libraries:repository:memory"))
    implementation("com.benasher44:uuid")
    implementation("com.soywiz.korlibs.klock:klock")
    implementation("com.zegreatrob.jsmints:minjson")
    implementation("io.github.microutils:kotlin-logging")
    implementation("io.ktor:ktor-client-logging")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-node")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation(npmConstrained("@aws-sdk/client-apigatewaymanagementapi"))
    implementation(npmConstrained("@aws-sdk/client-lambda"))
    implementation(npmConstrained("@graphql-tools/schema"))
    implementation(npmConstrained("@graphql-tools/stitch"))
    implementation(npmConstrained("body-parser"))
    implementation(npmConstrained("compression"))
    implementation(npmConstrained("cookie-parser"))
    implementation(npmConstrained("express"))
    implementation(npmConstrained("express-jwt"))
    implementation(npmConstrained("express-statsd"))
    implementation(npmConstrained("fs-extra"))
    implementation(npmConstrained("graphql"))
    implementation(npmConstrained("jose"))
    implementation(npmConstrained("jwks-rsa"))
    implementation(npmConstrained("method-override"))
    implementation(npmConstrained("mime"))
    implementation(npmConstrained("minimist"))
    implementation(npmConstrained("node-fetch"))
    implementation(npmConstrained("on-finished"))
    implementation(npmConstrained("parse5"))

    testImplementation(project(":libraries:stub-model"))
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation(npmConstrained("serverless"))
    testImplementation(npmConstrained("serverless-offline"))
    testImplementation(npmConstrained("serverless-offline-ssm"))
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
            "packageJson",
            ":kotlinNpmInstall",
            compileKotlinJs,
            processResources,
            compileProductionExecutableKotlinJs,
            "productionExecutableCompileSync",
        )
        mustRunAfter(clean)
        inputs.dir(processResources.map { it.destinationDir.path })
        inputs.file(compileProductionExecutableKotlinJs.map {
            it.destinationDirectory.file(it.compilerOptions.moduleName.map { "$it.js" })
        })
        inputs.file(file("webpack.config.js"))
        inputs.dir("public")
        outputs.dir(file("build/webpack-output"))
        outputs.cacheIf { true }
        val compilationName = "main"
        val jsProject: org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension = project.extensions.getByType()
        val compilation = jsProject.js().compilations.named(compilationName).get()

        inputs.file(compilation.npmProject.packageJsonFile)

        setup(project)
        nodeModulesDir = compilation?.npmProject?.nodeModulesDir
        npmProjectDir = compilation?.npmProject?.dir

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
        dependsOn(assemble, clientConfiguration, test, compileKotlinJs)
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
        val serverlessBuildDir = "${project.buildDir.absolutePath}/$stage/lambda-dist"
        setup(project)
        dependsOn(assemble, test, compileKotlinJs, ":calculateVersion")
        val releaseVersion = rootProject.version
        environment("CLIENT_URL" to "https://assets.zegreatrob.com/coupling/$releaseVersion")
        enabled = "$releaseVersion".run { !(contains("SNAPSHOT") || isBlank()) }
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
