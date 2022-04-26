package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class TagVersion : DefaultTask() {

    @Input
    lateinit var extension: GrgitServiceExtension

    @TaskAction
    fun execute() {
        val grgit = extension.service.get().grgit
        if (!project.version.toString().contains("SNAPSHOT")) {
            grgit.tag.add { name = "${project.version}" }
            grgit.push { tags = true }
        }
    }
}

open class Release : DefaultTask() {
    @TaskAction
    fun execute() {
        if (project.version.toString().contains("SNAPSHOT")) {
            throw GradleException("Cannot release a snapshot")
        }
    }
}