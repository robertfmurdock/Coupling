package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class CalculateVersion : DefaultTask() {

    @Input
    lateinit var extension: GrgitServiceExtension

    @Input
    var releaseBranch: String? = null

    @TaskAction
    fun execute() {
        val grgit = extension.service.get().grgit

        val version = grgit.calculateNextVersion() + if (grgit.canRelease())
            ""
        else
            "-SNAPSHOT"

        logger.quiet(version)
    }

    private fun Grgit.calculateNextVersion(): String {
        val description = describe {}
        val (previousVersionNumber) = description.split("-")
        val (major, minor, patch) = previousVersionNumber.substring(1).split(".")
        return "v${major}.${minor}.${patch.toInt() + 1}"
    }

    private fun Grgit.canRelease(): Boolean {
        val currentBranch = branch.current()
        val currentBranchStatus = branch.status { this.name = currentBranch.name }
        return status().isClean
            && currentBranchStatus.aheadCount == 0
            && currentBranchStatus.behindCount == 0
            && currentBranch.name == releaseBranch
    }
}
