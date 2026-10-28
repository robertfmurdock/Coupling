package com.zegreatrob.coupling.plugins

import org.jmailen.gradle.kotlinter.tasks.FormatTask
import org.jmailen.gradle.kotlinter.tasks.LintTask

repositories {
    mavenCentral()
}

plugins {
    id("org.jmailen.kotlinter")
}

tasks.withType<FormatTask>().configureEach {
    exclude { spec -> spec.file.absolutePath.contains("generated") }
}

tasks.withType<LintTask>().configureEach {
    exclude { spec -> spec.file.absolutePath.contains("generated") }
}
