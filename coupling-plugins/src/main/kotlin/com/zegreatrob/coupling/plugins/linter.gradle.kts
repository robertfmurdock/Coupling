package com.zegreatrob.coupling.plugins

repositories {
    mavenCentral()
}

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("0.45.2")
}
