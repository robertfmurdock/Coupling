import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization") version "1.3.61"
}

kotlin {

    targets {
        js { nodejs() }
        jvm()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2-1.3.60")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.2-1.3.60")
                implementation("com.soywiz.korlibs.klock:klock:1.8.1")
                implementation("io.github.microutils:kotlin-logging-common:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0-1.3.60-eap-76")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":json"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation(project(":test-logging"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("io.github.microutils:kotlin-logging:1.7.8")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.slf4j:slf4j-simple:1.7.5")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")

                implementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.2-1.3.60")
                implementation("io.github.microutils:kotlin-logging-js:1.7.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0-1.3.60-eap-76")
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

    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
        kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
    }

    val jvmTest by getting(Test::class) {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")

        useJUnitPlatform()
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}