package com.zegreatrob.coupling.plugins

import com.zegreatrob.tools.tagger.TaggerExtension

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js { nodejs() }
}

val serverProject: Project = project.project(":server")

val taggerExtension = TaggerExtension.apply(rootProject)

val deployDir = buildDir.resolve("deploy")

tasks {
    val copyServerYml by registering(Copy::class) {
        into(deployDir)
        from("${serverProject.projectDir.absolutePath}/serverless.yml")
    }
    val copyDeployResources by registering(Copy::class) {
        dependsOn(copyServerYml, ":server:assemble")
        into(buildDir.resolve("deploy/build/executable"))
        from(serverProject.buildDir.resolve("executable"))
    }

    val deploy by registering(NodeExec::class) {
        configureDeploy(project.name)
        dependsOn(":release", copyDeployResources)
        mustRunAfter(":server:check")
        mustRunAfter(":e2e:check")
    }
    taggerExtension.releaseProvider.configure {
        finalizedBy(deploy)
    }
}

fun NodeExec.configureDeploy(stage: String) {
    setup(project)
    mustRunAfter(
        ":release",
        ":client:uploadToS3",
    )
    if (("${rootProject.version}").run { contains("SNAPSHOT") || isBlank() }) {
        enabled = false
    }
    workingDir = deployDir
    nodeCommand = "serverless"
    arguments = listOf(
        "deploy",
        "--config",
        deployDir.resolve("serverless.yml").absolutePath,
        "--package",
        deployDir.absolutePath,
        "--stage",
        stage
    )
}
