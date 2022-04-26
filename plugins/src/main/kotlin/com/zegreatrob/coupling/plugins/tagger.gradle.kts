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
        this.calculateVersion = calculateVersion.get()
        this.version = project.version.toString()
        extension = grgitService
    }

    val release by registering(Release::class) {
        extension = grgitService
        this.calculateVersion = calculateVersion.get()
        this.version = project.version.toString()
        dependsOn(assemble, calculateVersion)
        mustRunAfter(check)
        finalizedBy(tag)
    }

}
