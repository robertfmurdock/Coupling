package com.zegreatrob.coupling.plugins

plugins {
    id("org.ajoberstar.grgit.service")
    base
}

tasks {
    val calculateVersion by registering(CalculateVersion::class) {
        this.extension = grgitService
    }
    check {
        dependsOn(calculateVersion)
    }

    val tag by registering(TagVersion::class) {
        extension = grgitService
    }

    val release by registering {
        dependsOn(assemble, calculateVersion)
        mustRunAfter(check)
        finalizedBy(tag)
    }

}
