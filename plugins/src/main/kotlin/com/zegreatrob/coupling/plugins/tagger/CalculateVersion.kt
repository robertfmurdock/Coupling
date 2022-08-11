package com.zegreatrob.coupling.plugins.tagger

import org.ajoberstar.grgit.Grgit
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.FileOutputStream

open class CalculateVersion : DefaultTask(), TaggerExtensionSyntax {

    @Input
    override lateinit var taggerExtension: TaggerExtension

    @Input
    var exportToGithubEnv: Boolean = false

    @TaskAction
    fun execute() {
        logger.quiet(taggerExtension.version)
        if (exportToGithubEnv) {
            FileOutputStream(System.getenv("GITHUB_ENV"), true)
                .write("COUPLING_VERSION=${taggerExtension.version}".toByteArray())
        }
    }
}

fun Grgit.calculateNextVersion(): String {
    val description = describe {}
    val (previousVersionNumber) = description.split("-")
    val (major, minor, patch) = previousVersionNumber.substring(1).split(".")
    return "v${major}.${minor}.${patch.toInt() + 1}"
}

fun Grgit.canRelease(releaseBranch: String?): Boolean {
    val currentBranch = branch.current()
    val currentBranchStatus = branch.status { this.name = currentBranch.name }
    return status().isClean
        && currentBranchStatus.aheadCount == 0
        && currentBranchStatus.behindCount == 0
        && currentBranch.name == releaseBranch
}