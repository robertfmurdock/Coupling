import com.zegreatrob.coupling.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version "1.4.30"
}

kotlin {

    targets {
        js {
            nodejs()
            useCommonJs()
        }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation(project(":logging"))
                implementation("com.zegreatrob.testmints:action:3.2.0")
                implementation("com.zegreatrob.testmints:action-async:3.2.0")
                implementation("com.benasher44:uuid:0.2.3")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                implementation("io.github.microutils:kotlin-logging:2.0.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":json"))
                implementation(project(":test-action"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("com.zegreatrob.testmints:standard:3.2.0")
                implementation("com.zegreatrob.testmints:minassert:3.2.0")
                implementation(project(":test-logging"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.0.4")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:1.7.5")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")

                implementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-test-js")
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