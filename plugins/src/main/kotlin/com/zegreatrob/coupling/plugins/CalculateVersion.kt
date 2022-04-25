package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

open class CalculateVersion : DefaultTask() {

    @Input
    var extension: GrgitServiceExtension? = null

    @Input
    var releaseBranch: String? = null

    @TaskAction
    fun execute() {
        val grgit = extension!!.service.get().grgit

        project.version = grgit.calculateNextVersion() + if (grgit.canRelease())
            ""
        else
            "-SNAPSHOT"

        logger.quiet("Version has been set to ${project.version}")
    }

    private fun Grgit.calculateNextVersion(): String {
        val description = describe {}

        val (previousVersionNumber) = description.split("-")
        val (major, minor, patch) = previousVersionNumber.substring(1).split(".")

        val nextVersion = "Z${major}.${minor}.${patch.toInt() + 1}"
        return nextVersion
    }

    private fun Grgit.canRelease(): Boolean {
        val currentBranch = branch.current()
        val currentBranchStatus = branch.status { this.name = currentBranch.name }

        return currentBranchStatus.aheadCount == 0
            && currentBranchStatus.behindCount == 0
            && currentBranch.name == releaseBranch
    }
}
