repositories {
    jcenter()
}

plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "1.3.20"
    id("com.jfrog.bintray") version "1.7.3"
    id("net.researchgate.release") version "2.6.0"
}

dependencies {
    implementation(kotlin("stdlib", "1.3.20"))
    implementation(kotlin("compiler", "1.3.20"))
    implementation(kotlin("reflect", "1.3.20"))
    implementation(kotlin("gradle-plugin", "1.3.20"))
    implementation(gradleApi())
    implementation(localGroovy())
}

