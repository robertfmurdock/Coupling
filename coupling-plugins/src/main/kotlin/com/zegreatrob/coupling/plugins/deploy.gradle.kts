package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.tools.tagger.ReleaseVersion
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        outputModuleName = "Coupling-deploy-${project.name}"
        nodejs()
    }
}

val serverProject: Project = project.project(":server")

val deployDir: Provider<Directory> = layout.buildDirectory.dir("deploy")
val serverlessPackageJson = rootProject.layout.projectDirectory.file("libraries/js-dependencies/package.json")
val serverlessFrameworkVersion = providers.fileContents(serverlessPackageJson).asText.map { packageJson ->
    Regex("\\\"serverless\\\"\\s*:\\s*\\\"[^0-9]*(\\d+\\.\\d+\\.\\d+)\\\"")
        .find(packageJson)
        ?.groupValues
        ?.get(1)
        ?: error("Unable to find the Serverless version in ${serverlessPackageJson.asFile}")
}

tasks {
    val copyServerYml = register<Copy>("copyServerYml") {
        into(deployDir)
        from("${serverProject.projectDir.absolutePath}/serverless.yml")
        inputs.file(serverlessPackageJson)
        doFirst {
            filter<ReplaceTokens>(
                "tokens" to mapOf("4" to "frameworkVersion: '${serverlessFrameworkVersion.get()}'"),
                "beginToken" to "frameworkVersion: '",
                "endToken" to "'",
            )
        }
    }
    val copyDeployConfigs = register<Copy>("copyDeployConfigs") {
        into(deployDir.map { it.dir("deploy") })
        from(serverProject.projectDir.resolve("deploy"))
    }
    val copyDeployResources = register<Copy>("copyDeployResources") {
        dependsOn(copyServerYml, copyDeployConfigs, ":server:assemble")
        into(layout.buildDirectory.dir("deploy/build/executable"))
        from(serverProject.layout.buildDirectory.dir("executable"))
    }

    fun NodeExec.configureServerless() {
        setup(project)
        environment("SERVERLESS_ACCESS_KEY", System.getenv("SERVERLESS_ACCESS_KEY"))
        taskWorkingDirectory.set(deployDir)
        nodeCommand.set("serverless")
    }

    val serverlessPackage = register<NodeExec>("serverlessPackage") {
        configureServerless()
        val releaseVersion = rootProject.version
        val serverlessBuildDir = serverProject.layout.buildDirectory.dir("${project.name}/lambda-dist")
        environment(
            "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/$releaseVersion",
            "CLI_URL" to "https://assets.zegreatrob.com/coupling-cli/$releaseVersion",
        )
        arguments.set(deployDir.zip(serverlessBuildDir) { directory, packageDirectory -> listOf(
            "package",
            "--verbose",
            "--config",
            directory.file("serverless.yml").asFile.absolutePath,
            "--package",
            packageDirectory.asFile.absolutePath,
            "--stage",
            project.name,
        ) })
        dependsOn(copyDeployResources, ":calculateVersion", ":server:serverlessWarmup")
    }

    val prune = register<NodeExec>("prune") {
        configureServerless()
        mustRunAfter(
            ":release",
            ":client:uploadToS3",
            ":server:check",
            ":e2e:check",
        )
        arguments.set(deployDir.map { directory -> listOf(
            "prune",
            "-n=10",
            "--config",
            directory.file("serverless.yml").asFile.absolutePath,
            "--stage",
            project.name,
        ) })
        dependsOn(copyDeployResources)
    }
    val deploy = register<NodeExec>("deploy") {
        configureServerless()
        mustRunAfter(
            ":release",
            ":client:uploadToS3",
            ":server:check",
            ":e2e:check",
        )
        dependsOn(prune)
        val serverlessBuildDir = serverProject.layout.buildDirectory.dir("${project.name}/lambda-dist")
        arguments.set(deployDir.zip(serverlessBuildDir) { directory, packageDirectory -> listOf(
            "deploy",
            "--config",
            directory.file("serverless.yml").asFile.absolutePath,
            "--package",
            packageDirectory.asFile.absolutePath,
            "--stage",
            project.name,
        ) })
        dependsOn(":release", serverlessPackage)
    }

    if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
        serverlessPackage { enabled = false }
        prune { enabled = false }
        deploy { enabled = false }
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release").configure {
            finalizedBy(prune, deploy)
        }
}
