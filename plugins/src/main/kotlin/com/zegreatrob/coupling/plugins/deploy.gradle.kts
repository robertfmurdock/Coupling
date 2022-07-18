package com.zegreatrob.coupling.plugins

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.node")
}

kotlin {
    js { nodejs() }
}

val serverProject: Project = project.project(":server")
fun serverlessBuildDir(stage: String) = "${serverProject.buildDir.absolutePath}/${stage}/lambda-dist"
val serverlessYmlPath = "${serverProject.projectDir.absolutePath}/serverless.yml"

tasks {
    val deploy by registering(NodeExec::class) {
        configureDeploy(project.name)
        dependsOn(":release")
        mustRunAfter(":server:check")
        mustRunAfter(":e2e:check")
    }
    val release by registering {
        dependsOn(":release", deploy)
    }
}

fun NodeExec.configureDeploy(stage: String) {
    setup(project)
    mustRunAfter(
        ":release",
        ":client:uploadToS3",
    )
    if (rootProject.version.toString().contains("SNAPSHOT")) {
        enabled = false
    }
    nodeCommand = "serverless"
    arguments = listOf(
        "deploy",
        "--config",
        serverlessYmlPath,
        "--package",
        serverlessBuildDir(stage),
        "--stage",
        stage
    )
}

