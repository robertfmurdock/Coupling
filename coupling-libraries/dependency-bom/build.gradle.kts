plugins {
    `java-platform`
    id("com.zegreatrob.coupling.plugins.versioning")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("0.45.2")
}

dependencies {
    constraints {
        api("org.slf4j:slf4j-simple:2.0.0")
        api("com.soywiz.korlibs.klock:klock:3.0.1")
        api("org.jetbrains.kotlinx:kotlinx-html-js:0.8.0")
        api("com.benasher44:uuid:0.5.0")
        api("io.github.microutils:kotlin-logging:2.1.23")
    }
}
