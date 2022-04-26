package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class TagVersion : DefaultTask() {

    @Input
    lateinit var extension: GrgitServiceExtension

    @Input
    lateinit var calculateVersion: CalculateVersion

    @Input
    lateinit var version: String

    @TaskAction
    fun execute() {
        val grgit = extension.service.get().grgit
        if (!version.contains("SNAPSHOT") && isOnReleaseBranch(grgit, calculateVersion)) {
            grgit.tag.add { name = version }
            grgit.push { tags = true }
        }
    }
}

open class Release : DefaultTask() {

    @Input
    lateinit var extension: GrgitServiceExtension

    @Input
    lateinit var calculateVersion: CalculateVersion

    @Input
    lateinit var version: String

    @TaskAction
    fun execute() {
        val grgit = extension.service.get().grgit
        if (isOnReleaseBranch(grgit, calculateVersion) && version.contains("SNAPSHOT")) {
            throw GradleException("Cannot release a snapshot")
        }
    }
}

private fun isOnReleaseBranch(grgit: Grgit, calculateVersion: CalculateVersion) =
    grgit.branch.current().name == calculateVersion.releaseBranch