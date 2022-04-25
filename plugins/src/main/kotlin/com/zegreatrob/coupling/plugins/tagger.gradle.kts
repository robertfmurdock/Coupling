package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.gradle.GrgitServiceExtension

plugins {
    id("org.ajoberstar.grgit.service")
    base
}

tasks {
    val calculateVersion by registering(CalculateVersion::class) {
        this.extension = grgitService
    }
    val check by getting {
        dependsOn(calculateVersion)
    }
}

