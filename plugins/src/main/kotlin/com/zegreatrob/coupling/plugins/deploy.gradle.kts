package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.NodeExec
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.invoke

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.node")
}

kotlin {
    js { nodejs() }
}

tasks {
    val deploy by creating(NodeExec::class) {
        configureDeploy(project.name)
    }

    findByPath(":release")!!.finalizedBy(deploy)
}


fun NodeExec.configureDeploy(stage: String) {
    val serverProject = project.project(":server")
    val serverlessBuildDir = "${serverProject.buildDir.absolutePath}/${stage}/lambda-dist"
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
        "${serverProject.projectDir.absolutePath}/serverless.yml",
        "--package",
        serverlessBuildDir,
        "--stage",
        stage
    )
}
