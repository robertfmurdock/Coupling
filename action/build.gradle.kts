import com.zegreatrob.coupling.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("kotlinx-serialization") version "1.6.0"
}

kotlin {

    targets {
        js {
            nodejs { testTask { useMocha { timeout = "10s" } } }
            useCommonJs()
        }
        jvm()
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
            }
        }

        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation(project(":logging"))
                implementation("com.zegreatrob.testmints:action:5.3.14")
                implementation("com.zegreatrob.testmints:action-async:5.3.14")
                implementation("com.benasher44:uuid:0.3.1")
                implementation("com.soywiz.korlibs.klock:klock:2.4.8")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                implementation("io.github.microutils:kotlin-logging:2.1.16")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":json"))
                implementation(project(":test-action"))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("com.zegreatrob.testmints:standard:5.3.14")
                implementation("com.zegreatrob.testmints:minassert:5.3.14")
                implementation(project(":test-logging"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.1.16")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha5")
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.1")
            }
        }
        val jsTest by getting {
            dependencies {
            }
        }
    }
}

tasks {

    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}