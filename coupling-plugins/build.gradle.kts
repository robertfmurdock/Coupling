import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    alias(libs.plugins.org.jmailen.kotlinter)
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

dependencies {
    implementation(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
    implementation(libs.com.github.ben.manes.gradle.versions.plugin)
    implementation(libs.com.soywiz.korlibs.klock)
    implementation(libs.org.ajoberstar.grgit.gradle.plugin)
    implementation(libs.org.apache.logging.log4j.log4j.core)
    implementation(libs.org.apache.logging.log4j.log4j.iostreams)
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.org.jmailen.gradle.kotlinter.gradle)
    implementation(libs.org.slf4j.slf4j.api)
    implementation(platform(libs.com.zegreatrob.testmints.testmints.bom))
    implementation("com.zegreatrob.testmints:mint-logs-plugin")
    implementation(platform(libs.com.zegreatrob.jsmints.jsmints.bom))
    implementation("com.zegreatrob.jsmints:plugins")
    implementation(platform(libs.com.zegreatrob.tools.tools.bom))
    implementation("com.zegreatrob.tools:tagger-plugin")
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-alpha.*|-dev.*)\$"
                .toRegex(RegexOption.IGNORE_CASE)
                .matches(candidate.version)
        }
    }
}

versionCatalogUpdate {
    sortByKey.set(true)
    keep {
        keepUnusedVersions.set(true)
        keepUnusedLibraries.set(true)
    }
}
