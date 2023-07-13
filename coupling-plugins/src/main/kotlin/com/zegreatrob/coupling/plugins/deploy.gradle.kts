package com.zegreatrob.coupling.plugins

import com.zegreatrob.tools.tagger.ReleaseVersion
import com.zegreatrob.tools.tagger.TaggerExtension

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

val taggerExtension: TaggerExtension = rootProject.extensions.getByType(TaggerExtension::class.java)

val deployDir = buildDir.resolve("deploy")

tasks {
    val copyServerYml by registering(Copy::class) {
        into(deployDir)
        from("${serverProject.projectDir.absolutePath}/serverless.yml")
    }
    val copyDeployConfigs by registering(Copy::class) {
        into(deployDir.resolve("deploy"))
        from(serverProject.projectDir.resolve("deploy"))
    }
    val copyDeployResources by registering(Copy::class) {
        dependsOn(copyServerYml, copyDeployConfigs, ":server:assemble")
        into(buildDir.resolve("deploy/build/executable"))
        from(serverProject.buildDir.resolve("executable"))
    }

    val deploy by registering(NodeExec::class) {
        configureDeploy(project.name)
        dependsOn(":release", copyDeployResources)
        mustRunAfter(":server:check")
        mustRunAfter(":e2e:check")
    }
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release").configure {
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
        serverProject.buildDir.resolve("${project.name}/lambda-dist").absolutePath,
        "--stage",
        stage
    )
}
