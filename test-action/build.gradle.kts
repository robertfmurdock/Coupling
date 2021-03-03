import com.zegreatrob.coupling.build.BuildConstants

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version "1.4.31"
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
                implementation(project(":action"))
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:action:3.3.3")
                implementation("com.zegreatrob.testmints:action-async:3.3.3")
                implementation("com.zegreatrob.testmints:async:3.3.3")
                implementation("com.zegreatrob.testmints:standard:3.3.3")
                implementation("com.zegreatrob.testmints:minassert:3.3.3")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")

                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")

                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:2.0.4")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.1.0")
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