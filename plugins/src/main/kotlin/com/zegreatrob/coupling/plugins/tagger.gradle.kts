package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.tagger.CalculateVersion
import com.zegreatrob.coupling.plugins.tagger.ReleaseVersion
import com.zegreatrob.coupling.plugins.tagger.TagVersion
import com.zegreatrob.coupling.plugins.tagger.TaggerExtension

plugins {
    id("org.ajoberstar.grgit.service")
    base
}

val tagger = project.extensions.create("tagger", TaggerExtension::class, grgitService, project)

tasks {
    val environmentFile = project.findProperty("environmentFile")
    val calculateVersion by registering(CalculateVersion::class) {
        taggerExtension = tagger
        environmentFile?.let {
            appendToFile = "$environmentFile"
        }
    }
    check {
        dependsOn(calculateVersion)
    }

    val tag by registering(TagVersion::class) {
        taggerExtension = tagger
    }

    register("release", ReleaseVersion::class) {
        taggerExtension = tagger
        dependsOn(assemble)
        mustRunAfter(check)
        finalizedBy(tag)
    }
}
