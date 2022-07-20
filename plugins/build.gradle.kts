import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.7.0")
    `kotlin-dsl`
    id("com.github.ben-manes.versions") version ("0.42.0")
    id("se.patrikerdes.use-latest-versions") version ("0.2.18")
}
val kotlinVersion = "1.7.10"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:1.7.10")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("se.patrikerdes:gradle-use-latest-versions-plugin:0.2.18")
    implementation("com.zegreatrob.jsmints.plugins.jspackage:com.zegreatrob.jsmints.plugins.jspackage.gradle.plugin:1.6.12")
    implementation("com.soywiz.korlibs.klock:klock:2.7.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.18.0")
    implementation("org.slf4j:slf4j-api:2.0.0-alpha7")
    implementation("org.jlleitschuh.gradle:ktlint-gradle:10.3.0")
    implementation("org.ajoberstar.grgit:org.ajoberstar.grgit.gradle.plugin:5.0.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.13.3")
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
