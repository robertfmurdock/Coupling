import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
            binaries.executable()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":logging"))
                implementation("com.zegreatrob.testmints:standard:4.1.11")
                implementation("com.zegreatrob.testmints:report:4.1.11")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging:2.0.10")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.github.microutils:kotlin-logging:2.0.10")
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha4")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

val testLoggingLib: Configuration by configurations.creating {
}

tasks {
    val compileProductionExecutableKotlinJs by getting(Kotlin2JsCompile::class) {}
    artifacts {
        add(testLoggingLib.name, compileProductionExecutableKotlinJs.outputFile) {
            builtBy(compileProductionExecutableKotlinJs)
        }
    }
}