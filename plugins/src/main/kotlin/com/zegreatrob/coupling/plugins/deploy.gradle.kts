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
    val buildDeployBundle by creating(NodeExec::class) {
        configureBuild(project.name)
    }
    val deploy by creating(NodeExec::class) {
        dependsOn(buildDeployBundle)
        configureDeploy(project.name)
    }
    findByPath(":release")!!.finalizedBy(deploy)
}

fun NodeExec.configureBuild(stage: String) {
    dependsOn(":server:assemble", ":server:test", ":server:compileKotlinJs")
    environment(
        "SERVER_DIR" to serverProject.projectDir.absolutePath,
        "AWS_ACCESS_KEY_ID" to (System.getenv("AWS_ACCESS_KEY_ID") ?: "fake"),
        "AWS_SECRET_ACCESS_KEY" to (System.getenv("AWS_SECRET_ACCESS_KEY") ?: "fake"),
        "CLIENT_URL" to "https://assets.zegreatrob.com/coupling/${project.version}",
    )
    nodeCommand = "serverless"
    arguments = listOf(
        "package",
        "--config",
        serverlessYmlPath,
        "--package",
        serverlessBuildDir(stage),
        "--stage",
        stage
    )
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

