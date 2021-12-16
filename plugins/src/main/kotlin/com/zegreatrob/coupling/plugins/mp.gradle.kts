package com.zegreatrob.coupling.plugins

plugins {
    kotlin("multiplatform")
}

dependencies {
    "commonMainImplementation"(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:5.3.15"))
}
