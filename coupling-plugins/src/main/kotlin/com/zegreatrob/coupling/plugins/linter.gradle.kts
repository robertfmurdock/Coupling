package com.zegreatrob.coupling.plugins

import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

repositories {
    mavenCentral()
}

plugins {
    id("org.jmailen.kotlinter")
}

afterEvaluate {
    tasks {
        withType<FormatTask> {
            exclude { spec -> spec.file.absolutePath.contains("generated") }
        }
        withType<LintTask> {
            exclude { spec -> spec.file.absolutePath.contains("generated") }
        }
    }
}