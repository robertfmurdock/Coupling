plugins {
    `java-platform`
    id("com.zegreatrob.coupling.plugins.versioning")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    gradlePluginPortal()
}

ktlint {
    version.set("0.45.2")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.com.zegreatrob.jsmints.jsmints.bom))
    api(platform(libs.com.zegreatrob.testmints.testmints.bom))
    api(platform(libs.io.ktor.ktor.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.serialization.bom))
    api(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    api(platform(libs.org.junit.junit.bom))
    constraints {
        api(libs.com.benasher44.uuid)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
        api(libs.com.soywiz.korlibs.klock)
        api(libs.io.github.microutils.kotlin.logging)
        api(libs.org.slf4j.slf4j.simple)
        api(libs.org.jetbrains.kotlinx.kotlinx.datetime)
        api(libs.org.jetbrains.kotlinx.kotlinx.html.js)
    }
}
