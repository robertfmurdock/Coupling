
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
    id("com.zegreatrob.coupling.plugins.node")
    id("com.zegreatrob.coupling.plugins.serialization")
}

kotlin {
    js {
        nodejs { testTask { useMocha { timeout = "10s" } } }
    }
    sourceSets {
        val main by getting
        val test by getting {
            dependsOn(main)
        }
        all { languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi") }
    }
}

dependencies {
    implementation(project(":model"))
    implementation(project(":repository-core"))
    implementation("com.zegreatrob.testmints:minjson")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    implementation(project(":json"))
    implementation("io.ktor:ktor-client-core:1.6.7")
    implementation("io.ktor:ktor-client-serialization:1.6.7")
    implementation("io.ktor:ktor-client-logging:1.6.7")
    implementation("com.soywiz.korlibs.klock:klock:2.4.12")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")

    testImplementation(project(":repository-validation"))
    testImplementation(project(":test-logging"))
    testImplementation(project(":stub-model"))
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.benasher44:uuid:0.4.0")
    testImplementation("com.zegreatrob.testmints:standard")
    testImplementation("com.zegreatrob.testmints:minassert")
    testImplementation("com.zegreatrob.testmints:async")
    testImplementation("com.zegreatrob.testmints:minassert")
}

tasks {
    "nodeTest" {
        dependsOn(":composeUp")
    }
}
