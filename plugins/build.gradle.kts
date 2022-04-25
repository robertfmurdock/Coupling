import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.20")
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version ("0.42.0")
    id("se.patrikerdes.use-latest-versions") version ("0.2.18")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin"))
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:1.6.21")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("se.patrikerdes:gradle-use-latest-versions-plugin:0.2.18")
    implementation("com.soywiz.korlibs.klock:klock:2.7.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.17.2")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha7")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.2.1")
    implementation("org.ajoberstar.grgit:org.ajoberstar.grgit.gradle.plugin:5.0.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.2.2")
}

tasks {
    withType<DependencyUpdatesTask>().configureEach {
        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
        revision = "release"

        rejectVersionIf {
            "^[0-9.]+[0-9](-RC|-M[0-9]+|-RC[0-9]+)\$"
                .toRegex()
                .matches(candidate.version)
        }
    }
}
