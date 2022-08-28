package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.tagger.TaggerExtension

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js { nodejs() }
}

val serverProject: Project = project.project(":server")
fun serverlessBuildDir(stage: String) = "${serverProject.buildDir.absolutePath}/${stage}/lambda-dist"
val serverlessYmlPath = "${serverProject.projectDir.absolutePath}/serverless.yml"

val taggerExtension = TaggerExtension.apply(rootProject)

tasks {
    val deploy by registering(NodeExec::class) {
        configureDeploy(project.name)
        dependsOn(":release")
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

