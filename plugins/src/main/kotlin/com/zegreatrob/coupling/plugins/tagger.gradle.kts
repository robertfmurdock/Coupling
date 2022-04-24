package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.gradle.GrgitServiceExtension

plugins {
    id("org.ajoberstar.grgit.service")
    base
}

tasks {
    val describe by registering(DescribeTask::class) {
        this.extension = grgitService
    }
    val check by getting {
        dependsOn(describe)
    }
}

open class DescribeTask : DefaultTask() {

    @Input
    var extension: GrgitServiceExtension? = null

    @TaskAction
    fun execute() {
        val grgit = extension!!.service.get().grgit
        val description = grgit.describe {}
        println(description)

        val (previousVersionNumber, commitDistanceFromTag, dirty) = description.split("-")

        println(previousVersionNumber)

        val (major, minor, patch) = previousVersionNumber.substring(1).split(".")

        val nextVersion = "v${major}.${minor}.${patch.toInt() + 1}"
        println("prospective next version number $nextVersion")

        val isClean = dirty.isBlank()
        println("is clean $isClean")
    }
}
