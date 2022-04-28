package com.zegreatrob.coupling.plugins.tagger

import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

interface TaggerExtensionSyntax {
    var taggerExtension: TaggerExtension
    @get:Internal
    val grgit get() = taggerExtension.grgitServiceExtension.service.get().grgit
    @get:Internal
    val releaseBranch get() = taggerExtension.releaseBranch
    @get:Internal
    val version get() = taggerExtension.version

    @Internal
    fun isSnapshot() = version.contains("SNAPSHOT")
    @Internal
    fun isOnReleaseBranch(grgit: Grgit, releaseBranch: String?) = grgit.branch.current().name == releaseBranch
}

open class TagVersion : DefaultTask(), TaggerExtensionSyntax {

    @Input
    override lateinit var taggerExtension: TaggerExtension

    @TaskAction
    fun execute() {
        if (!isSnapshot() && isOnReleaseBranch(grgit, releaseBranch)) {
            this.grgit.tag.add { name = version }
            this.grgit.push { tags = true }
        }
    }
}

open class ReleaseVersion : DefaultTask(), TaggerExtensionSyntax {

    @Input
    override lateinit var taggerExtension: TaggerExtension

    @TaskAction
    fun execute() {
        val grgit = taggerExtension.grgitServiceExtension.service.get().grgit
        if (isOnReleaseBranch(grgit, taggerExtension.releaseBranch) && isSnapshot()) {
            throw GradleException("Cannot release a snapshot")
        }
    }
}