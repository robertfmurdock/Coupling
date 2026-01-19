plugins {
    `java-platform`
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    gradlePluginPortal()
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform(libs.com.zegreatrob.jsmints.jsmints.bom))
    api(platform(libs.com.zegreatrob.testmints.testmints.bom))
    api(platform(libs.com.zegreatrob.tools.tools.bom))
    api(platform(libs.io.ktor.ktor.bom))
    api(platform(libs.org.jetbrains.kotlin.wrappers.kotlin.wrappers.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.coroutines.bom))
    api(platform(libs.org.jetbrains.kotlinx.kotlinx.serialization.bom))
    api(platform(libs.org.junit.junit.bom))
    api(platform(libs.org.kotlincrypto.hash.bom))
    constraints {
        api(libs.com.apollographql.adapters.apollo.adapters.core)
        api(libs.com.apollographql.adapters.apollo.adapters.kotlinx.datetime)
        api(libs.com.apollographql.apollo.apollo.runtime)
        api(libs.com.apollographql.ktor.apollo.engine.ktor)
        api(libs.com.auth0.java.jwt)
        api(libs.com.fasterxml.jackson.core.jackson.databind)
        api(libs.com.github.ajalt.clikt.clikt)
        api(libs.com.lemonappdev.konsist)
        api(libs.io.github.oshai.kotlin.logging)
        api(libs.org.jetbrains.kotlin.wrappers.kotlin.react.router.dom.legacy)
        api(libs.org.jetbrains.kotlinx.kotlinx.datetime)
        api(libs.org.kotools.types)
        api(libs.org.kotools.types.kotlinx.serialization)
        api(libs.org.slf4j.slf4j.simple)
    }
}
