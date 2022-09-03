plugins {
    `java-platform`
    id("com.zegreatrob.coupling.plugins.versioning")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    gradlePluginPortal()
}

ktlint {
    version.set("0.45.2")
}

javaPlatform {
    allowDependencies()
}

dependencies {
    api(platform("com.zegreatrob.jsmints:jsmints-bom:1.6.36"))
    api(platform("com.zegreatrob.testmints:testmints-bom:8.1.10"))
    api(platform("io.ktor:ktor-bom:2.1.0"))
    api(platform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.4.0"))
    api(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4"))
    api(platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.382"))
    api(platform("org.junit:junit-bom:5.9.0"))
    constraints {
        api("com.benasher44:uuid:0.5.0")
        api("com.fasterxml.jackson.core:jackson-databind:2.13.3")
        api("com.soywiz.korlibs.klock:klock:3.0.0")
        api("io.github.microutils:kotlin-logging:2.1.23")
        api("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0")
        api("org.slf4j:slf4j-simple:2.0.0")
        api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    }
}
