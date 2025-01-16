package com.zegreatrob.coupling.plugins

import com.zegreatrob.tools.tagger.ReleaseVersion

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        moduleName = "Coupling-deploy-${project.name}"
        nodejs()
    }
}

val serverProject: Project = project.project(":server")

val deployDir: Provider<Directory> = layout.buildDirectory.dir("deploy")

tasks {
    val copyServerYml by registering(Copy::class) {
        into(deployDir)
        from("${serverProject.projectDir.absolutePath}/serverless.yml")
    }
    val copyDeployConfigs by registering(Copy::class) {
        into(deployDir.map { it.dir("deploy") })
        from(serverProject.projectDir.resolve("deploy"))
    }
    val copyDeployResources by registering(Copy::class) {
        dependsOn(copyServerYml, copyDeployConfigs, ":server:assemble")
        into(layout.buildDirectory.dir("deploy/build/executable"))
        from(serverProject.layout.buildDirectory.dir("executable"))
    }

    val prune by registering(NodeExec::class) {
        setup(project)
        mustRunAfter(
            ":release",
            ":client:uploadToS3",
            ":server:check",
            ":e2e:check"
        )
        environment("SERVERLESS_ACCESS_KEY", System.getenv("SERVERLESS_ACCESS_KEY"))
        workingDir = deployDir.get().asFile
        nodeCommand = "serverless"
        arguments = listOf(
            "prune",
            "-n=10",
            "--config",
            deployDir.get().file("serverless.yml").asFile.absolutePath,
            "--stage",
            project.name
        )
        dependsOn(copyDeployResources)
    }
    val deploy by registering(NodeExec::class) {
        setup(project)
        environment("SERVERLESS_ACCESS_KEY", System.getenv("SERVERLESS_ACCESS_KEY"))
        mustRunAfter(
            ":release",
            ":client:uploadToS3",
            ":server:check",
            ":e2e:check"
        )
        dependsOn(prune)
        workingDir = deployDir.get().asFile
        nodeCommand = "serverless"
        arguments = listOf(
            "deploy",
            "--config",
            deployDir.get().file("serverless.yml").asFile.absolutePath,
            "--package",
            serverProject.layout.buildDirectory.dir("${project.name}/lambda-dist").get().asFile.absolutePath,
            "--stage",
            project.name
        )
        dependsOn(":release", copyDeployResources)
    }

    if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
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

