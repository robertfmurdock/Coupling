import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerLogsContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.zegreatrob.coupling.build.loadPackageJson
import com.zegreatrob.coupling.build.nodeBinDir
import com.zegreatrob.coupling.build.nodeExec
import com.zegreatrob.coupling.build.nodeModulesDir
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("com.bmuschko.docker-remote-api")
    id("kotlinx-serialization") version "1.5.10"
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
    implementation(project(":repository-dynamo"))
    implementation(project(":repository-memory"))
    implementation(project(":server_action"))
    implementation("com.zegreatrob.testmints:minjson:4.0.12")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("com.soywiz.korlibs.klock:klock:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.2.1")
    implementation("com.benasher44:uuid:0.3.0")

    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
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
        dependsOn("test")
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
        dependsOn(assemble, clientConfiguration, "test")
        nodeExec(
            compileKotlinJs,
            listOf("$nodeModulesDir/.bin/serverless", "offline", "--config", project.relativePath("serverless.yml"))
        )
        environment("NODE_ENV", "production")
        environment(
            "CLIENT_PATH",
            System.getenv("CLIENT_PATH")
                ?.let { it.ifEmpty { null } }
                ?: "${file("${rootProject.rootDir.absolutePath}/client/build/distributions")}"
        )
    }

    val serverlessBuildDir = "${buildDir.absolutePath}/lambda-dist"

    val buildServerlessBuildImage by creating(DockerBuildImage::class) {
        dependsOn(assemble, clientConfiguration)
        inputDir.set(file("./"))
        remove.set(false)
        buildArgs.put(
            "STAGE", serverlessStage
        )
    }

    val serverlessBuildContainer by creating(DockerCreateContainer::class) {
        dependsOn(buildServerlessBuildImage)
        targetImageId(buildServerlessBuildImage.imageId)
        envVars.set(
            mutableMapOf(
                "AWS_ACCESS_KEY_ID" to (System.getenv("AWS_ACCESS_KEY_ID") ?: "fake"),
                "AWS_SECRET_ACCESS_KEY" to (System.getenv("AWS_SECRET_ACCESS_KEY") ?: "fake"),
                "CLIENT_PATH" to "https://assets.zegreatrob.com/coupling/${version}",
            )
        )
        attachStdout.set(true)
        hostConfig.autoRemove.set(true)
        hostConfig.binds.set(mutableMapOf(buildDir.absolutePath to "/usr/src/app/server/build"))
    }
    val serverlessBuildRunContainer by creating(DockerStartContainer::class) {
        dependsOn(serverlessBuildContainer, assemble)
        targetContainerId(serverlessBuildContainer.containerId)
    }
    val serverlessBuildWaitContainer by creating(DockerLogsContainer::class) {
        dependsOn(serverlessBuildRunContainer)
        follow.set(true)
        targetContainerId(serverlessBuildContainer.containerId)
    }
    val serverlessBuild by creating {
        dependsOn(serverlessBuildWaitContainer, "test")
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
        add(appConfiguration.name, compileKotlinJs.outputFile) {
            builtBy(compileKotlinJs)
        }
        add(appConfiguration.name, file("build/executable")) {
            builtBy(serverCompile)
        }
    }
}

val serverlessStage
    get() = if ("$version".contains("SNAPSHOT")) {
        "dev"
    } else {
        "prod"
    }