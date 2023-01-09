package com.zegreatrob.coupling.plugins.tagger

import com.zegreatrob.coupling.plugins.TaggerPlugin
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskProvider

open class TaggerExtension(val grgitServiceExtension: GrgitServiceExtension, @Transient val rootProject: Project) {

    @Input
    var releaseBranch: String? = null

    val version by lazy { calculateBuildVersion(grgitServiceExtension.service.get().grgit, releaseBranch) }

    val releaseProvider: TaskProvider<ReleaseVersion>
        get() = rootProject
            .tasks
            .withType(ReleaseVersion::class.java)
            .named("release")

    private fun calculateBuildVersion(grgit: Grgit, releaseBranch: String?) = grgit.calculateNextVersion() +
        if (grgit.canRelease(releaseBranch))
            ""
        else
            "-SNAPSHOT"

    companion object {
        fun apply(rootProject: Project): TaggerExtension {
            check(rootProject == rootProject.rootProject)
            rootProject.plugins.apply(TaggerPlugin::class.java)
            return rootProject.extensions.getByName("tagger") as TaggerExtension
        }
    }
}
