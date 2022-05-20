package com.zegreatrob.coupling.plugins

import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.repositories

repositories {
    mavenCentral()
}

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("0.45.2")
}
