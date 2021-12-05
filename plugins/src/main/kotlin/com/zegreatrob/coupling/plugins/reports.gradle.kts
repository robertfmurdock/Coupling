package com.zegreatrob.coupling.plugins

repositories {
    mavenCentral()
}

tasks {
    val projectResultPath = "${rootProject.buildDir.path}/test-output/${project.path}/results".replace(":", "/")

    val copyReportsToCircleCIDirectory by creating(Copy::class) {
        from("build/reports")
        into(projectResultPath)
    }
    val copyTestResultsToCircleCIDirectory by creating(Copy::class) {
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
