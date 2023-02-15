package com.zegreatrob.coupling.plugins.tagger

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
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
        val githubEnvFile = System.getenv("GITHUB_ENV")
        if (exportToGithubEnv && githubEnvFile != null) {
            FileOutputStream(githubEnvFile, true)
                .write("COUPLING_VERSION=${taggerExtension.version}".toByteArray())
        }
    }
}

fun Grgit.calculateNextVersion(): String {
    val description = describe {} ?: "-0.0.0"
    val (previousVersionNumber) = description.split("-")
    val (major, minor, patch) = previousVersionNumber.substring(1).split(".")
    return "v$major.$minor.${patch.toInt() + 1}"
}

fun Grgit.canRelease(releaseBranch: String?): Boolean {
    val currentBranch = branch.current()

    val currentBranchStatus = kotlin.runCatching { branch.status { this.name = currentBranch.name } }
        .getOrNull()
    return if (currentBranchStatus == null)
        false
    else
        status().isClean &&
        currentBranchStatus.aheadCount == 0 &&
        currentBranchStatus.behindCount == 0 &&
        currentBranch.name == releaseBranch
}

fun Grgit.tagReport() = tag.list()
    .filter { it.dateTime != null }
    .groupBy { tag ->
    "${tag.dateTime?.year} Week ${tag.weekNumber()}"
}.toSortedMap()

    .map {
        "${it.key} has ${it.value.size} tags [${it.value.joinToString { tag -> tag.name }}]"
    }
    .joinToString("\n")

private fun Tag.weekNumber() = "${(dateTime?.dayOfYear ?: 0) / 7}".let {
    if (it.length == 1) {
        "0$it"
    } else {
        it
    }
}
