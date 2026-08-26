package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.tools.tagger.ReleaseVersion

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

tasks {
    val copyServerYml = register<Copy>("copyServerYml") {
        into(deployDir)
        from("${serverProject.projectDir.absolutePath}/serverless.yml")
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
        workingDir = deployDir.get().asFile
        nodeCommand = "serverless"
    }

    val serverlessPackage = register<NodeExec>("serverlessPackage") {
        configureServerless()
        val releaseVersion = rootProject.version
        val serverlessBuildDir = serverProject.layout.buildDirectory.dir("${project.name}/lambda-dist")
        environment(
            "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/$releaseVersion",
            "CLI_URL" to "https://assets.zegreatrob.com/coupling-cli/$releaseVersion",
        )
        arguments = listOf(
            "package",
            "--verbose",
            "--config",
            deployDir.get().file("serverless.yml").asFile.absolutePath,
            "--package",
            serverlessBuildDir.get().asFile.absolutePath,
            "--stage",
            project.name,
        )
        dependsOn(copyDeployResources, ":calculateVersion")
    }

    val prune = register<NodeExec>("prune") {
        configureServerless()
        mustRunAfter(
            ":release",
            ":client:uploadToS3",
            ":server:check",
            ":e2e:check",
        )
        arguments = listOf(
            "prune",
            "-n=10",
            "--config",
            deployDir.get().file("serverless.yml").asFile.absolutePath,
            "--stage",
            project.name,
        )
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
        arguments = listOf(
            "deploy",
            "--config",
            deployDir.get().file("serverless.yml").asFile.absolutePath,
            "--package",
            serverProject.layout.buildDirectory.dir("${project.name}/lambda-dist").get().asFile.absolutePath,
            "--stage",
            project.name,
        )
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
