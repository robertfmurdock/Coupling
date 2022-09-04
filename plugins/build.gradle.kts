import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    id("org.jlleitschuh.gradle.ktlint") version libs.versions.org.jlleitschuh.gradle.ktlint.get()
    alias(libs.plugins.com.github.ben.manes.versions)
    alias(libs.plugins.nl.littlerobots.version.catalog.update)
}

ktlint {
    version.set(libs.versions.ktlint.get())

    filter {
        exclude { element -> element.file.name != "build.gradle.kts" }
    }
}

dependencies {
    implementation(libs.org.jetbrains.kotlin.kotlin.stdlib)
    implementation(kotlin("gradle-plugin", libs.versions.kotlin.get()))
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.com.github.ben.manes.gradle.versions.plugin)
    implementation(libs.com.zegreatrob.jsmints.plugins.jspackage.gradle.plugin)
    implementation(libs.com.soywiz.korlibs.klock)
    implementation(libs.org.apache.logging.log4j.log4j.core)
    implementation(libs.org.apache.logging.log4j.log4j.iostreams)
    implementation(libs.org.slf4j.slf4j.api)
    implementation(libs.org.jlleitschuh.gradle.ktlint.gradle)
    implementation(libs.org.ajoberstar.grgit.gradle.plugin)
    implementation(libs.com.fasterxml.jackson.core.jackson.databind)
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+|-beta.*|-alpha.*)\$"
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
