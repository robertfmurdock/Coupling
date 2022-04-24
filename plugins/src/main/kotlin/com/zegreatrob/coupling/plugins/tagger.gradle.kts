package com.zegreatrob.coupling.plugins

import org.ajoberstar.grgit.gradle.GrgitService

plugins {
    id("org.ajoberstar.grgit.service")
}

tasks {
    val describe by registering(DescribeTask::class) {

    }
}

class DescribeTask @Inject constructor(@Internal val service: Property<GrgitService>) : DefaultTask() {

    @TaskAction
    fun execute() {
        println(service.get().grgit.describe())
    }
}
