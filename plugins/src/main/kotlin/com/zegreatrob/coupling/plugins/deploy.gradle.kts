package com.zegreatrob.coupling.plugins

import org.gradle.kotlin.dsl.*

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
    val deploy by creating(NodeExec::class) {
        configureDeploy(project.name)
    }
    findByPath(":release")!!.finalizedBy(deploy)
}

fun NodeExec.configureDeploy(stage: String) {
    mustRunAfter(
        ":release",
        ":updateGithubRelease",
        ":client:uploadToS3",
    )
    if (version.toString().contains("SNAPSHOT")) {
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

