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
    api(platform(libs.jsmints.bom))
    api(platform(libs.testmints.bom))
    api(platform(libs.tools.bom))
    api(platform(libs.ktor.bom))
    api(platform(libs.kotlin.wrappers.bom))
    api(platform(libs.kotlinx.coroutines.bom))
    api(platform(libs.kotlinx.serialization.bom))
    api(platform(libs.junit.bom))
    api(platform(libs.kotlincrypto.hash.bom))
    constraints {
        api(libs.apollo.adapters.core)
        api(libs.apollo.adapters.kotlinx.datetime)
        api(libs.apollo.runtime)
        api(libs.apollo.ktor.engine)
        api(libs.auth0.java.jwt)
        api(libs.jackson.databind)
        api(libs.clikt)
        api(libs.ksp.symbol.processing.api) {
            version {
                strictly(libs.versions.ksp.get())
            }
        }
        api(libs.konsist)
        api(libs.kotlin.test)
        api(libs.ksafe)
        api(libs.kotlin.logging)
        api(libs.kotlin.react.popper)
        api(libs.kotlinx.datetime)
        api(libs.kotools.types)
        api(libs.kotools.types.kotlinx.serialization)
        api(libs.slf4j.simple)
    }
}
