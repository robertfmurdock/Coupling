import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.6.0")
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version ("0.39.0")
    id("se.patrikerdes.use-latest-versions") version ("0.2.18")
}

val kotlinVersion = "1.6.0"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("se.patrikerdes:gradle-use-latest-versions-plugin:0.2.18")
    implementation("com.soywiz.korlibs.klock:klock:2.4.8")
    implementation("org.apache.logging.log4j:log4j-core:2.16.0")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
}

tasks {
    withType<DependencyUpdatesTask> {
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
