package com.zegreatrob.coupling.plugins

import com.zegreatrob.tools.TaggerPlugin
import com.zegreatrob.tools.tagger.ReleaseVersion

import nl.littlerobots.vcu.plugin.versionSelector

plugins {
    id("nl.littlerobots.version-catalog-update")
    base
}

repositories {
    mavenCentral()
}

versionCatalogUpdate {
    val rejectRegex = "^[0-9.]+[0-9](-RC|-M[0-9]*|-RC[0-9]*.*|-beta.*|-Beta.*|-alpha.*|-dev.*)$".toRegex()
    versionSelector { versionCandidate ->
        !rejectRegex.matches(versionCandidate.candidate.version)
    }
}


rootProject.apply<TaggerPlugin>()

tasks {
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release").configure {
            dependsOn(check)
        }
}
