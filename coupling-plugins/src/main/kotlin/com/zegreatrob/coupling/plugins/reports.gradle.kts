package com.zegreatrob.coupling.plugins

repositories {
    mavenCentral()
}

tasks {
    val projectResultPath = "${rootProject.buildDir.path}/test-output/${project.path}/results".replace(":", "/")

    val check = named("check")
    val copyReportsToCircleCIDirectory by registering(Copy::class) {
        mustRunAfter(check)
        from("build/reports")
        into(projectResultPath)
    }
    val copyTestResultsToCircleCIDirectory by registering(Copy::class) {
        mustRunAfter(check)
        from("build/test-results")
        into(projectResultPath)
    }
    register("collectResults") {
        dependsOn(copyReportsToCircleCIDirectory, copyTestResultsToCircleCIDirectory)
    }
}

afterEvaluate {
    mkdir(file(rootProject.buildDir.toPath().resolve("test-output")))
}
