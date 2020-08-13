plugins {
    kotlin("js")
}

kotlin {
    target {
        nodejs {}
        useCommonJs()
    }

    sourceSets {
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.110-kotlin-1.3.72")
    implementation("com.soywiz.korlibs.klock:klock:1.10.6")
    implementation("io.github.microutils:kotlin-logging-js:1.8.3")
    implementation(npm("@log4js-node/log4js-api"))
}

tasks {
}
