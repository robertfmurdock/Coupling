package com.zegreatrob.coupling.plugins.tagger

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension
import org.gradle.api.tasks.Input

open class TaggerExtension(val grgitServiceExtension: GrgitServiceExtension) {

    @Input
    var releaseBranch: String? = null

    val version by lazy {
        calculateBuildVersion(grgitServiceExtension.service.get().grgit, releaseBranch)
    }

    private fun calculateBuildVersion(grgit: Grgit, releaseBranch: String?) = grgit.calculateNextVersion() +
        if (grgit.canRelease(releaseBranch))
            ""
        else
            "-SNAPSHOT"


}
