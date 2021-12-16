package com.zegreatrob.coupling.plugins

plugins {
    kotlin("multiplatform")
}

dependencies {
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:5.3.15"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.1"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.2"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.281-kotlin-1.6.10"))
}
