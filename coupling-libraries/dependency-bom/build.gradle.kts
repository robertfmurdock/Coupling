plugins {
    `java-platform`
    id("com.zegreatrob.coupling.plugins.versioning")
    id("org.jmailen.kotlinter")
    id("com.zegreatrob.jsmints.plugins.ncu")
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    gradlePluginPortal()
}
kotlin { js { nodejs() } }

javaPlatform {
    allowDependencies()
}

dependencies {
    api(enforcedPlatform(libs.com.zegreatrob.jsmints.jsmints.bom))
    api(enforcedPlatform(libs.com.zegreatrob.testmints.testmints.bom))
    api(enforcedPlatform(libs.io.ktor.ktor.bom))
    api(enforcedPlatform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    api(enforcedPlatform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(enforcedPlatform(libs.org.jetbrains.kotlinx.kotlinx.serialization.bom))
    api(enforcedPlatform(libs.org.junit.junit.bom))
    constraints {
        api(libs.com.benasher44.uuid)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
        api(libs.com.soywiz.korlibs.klock)
        api(libs.io.github.microutils.kotlin.logging)
        api(libs.org.jetbrains.kotlinx.kotlinx.datetime)
        api(libs.org.jetbrains.kotlinx.kotlinx.html.js)
        api(libs.org.slf4j.slf4j.simple)
    }
}
