repositories {
    jcenter()
}

plugins {
    id("java")
    id("java-gradle-plugin")
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm").version ("1.3.21")
    id("com.jfrog.bintray") version "1.7.3"
    id("net.researchgate.release") version "2.6.0"
}


val kotlinVersion = "1.3.21"

dependencies {
    implementation(kotlin("stdlib", kotlinVersion))
    implementation(kotlin("compiler", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))
    implementation(kotlin("gradle-plugin", kotlinVersion))
    implementation(gradleApi())
    implementation(localGroovy())
}

