package com.zegreatrob.coupling.plugins

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.zegreatrob.tools.TaggerPlugin
import com.zegreatrob.tools.tagger.ReleaseVersion

plugins {
    base
    id("com.github.ben-manes.versions")
}

rootProject.apply<TaggerPlugin>()

tasks {
    rootProject
        .tasks
        .withType(ReleaseVersion::class.java)
        .named("release").configure {
        dependsOn(check)
    }

    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-alpha.*|-dev.*)\$"
                .toRegex(RegexOption.IGNORE_CASE)
                .matches(candidate.version)
        }
    }
}
