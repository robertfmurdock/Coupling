package com.zegreatrob.coupling.plugins

plugins {
    kotlin("multiplatform")
}

dependencies {
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:5.3.15"))
    "commonMainImplementation"(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.1"))
}
