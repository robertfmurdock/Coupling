package com.zegreatrob.coupling.plugins

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
}

dependencies {
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:5.5.2"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.1"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.2"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.282-kotlin-1.6.10"))
}
