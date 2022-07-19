package com.zegreatrob.coupling.plugins

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.zegreatrob.coupling.plugins.tagger.TaggerExtension

plugins {
    base
    id("se.patrikerdes.use-latest-versions")
    id("com.github.ben-manes.versions")
}

val taggerExtension = TaggerExtension.apply(rootProject)

tasks {
    taggerExtension.releaseProvider.configure {
        dependsOn(check)
    }

    withType<DependencyUpdatesTask> {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-alpha.*)\$"
                .toRegex(RegexOption.IGNORE_CASE)
                .matches(candidate.version)
        }
    }
}
