repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm").version("1.5.10")
    id("net.researchgate.release") version "2.6.0"
}

val kotlinVersion = "1.5.21"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("compiler", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.github.node-gradle:gradle-node-plugin:1.3.0")
    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("com.soywiz.korlibs.klock:klock:2.1.0")
    implementation("org.apache.logging.log4j:log4j-core:2.13.1")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0-rc1")
}
