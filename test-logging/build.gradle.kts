import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
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
                implementation("com.zegreatrob.coupling.libraries:logging")
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:report")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation("com.soywiz.korlibs.klock:klock:2.4.13")
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
                implementation("io.github.microutils:kotlin-logging:2.1.21")
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha6")
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
        add(testLoggingLib.name, compileProductionExecutableKotlinJs.outputFileProperty) {
            builtBy(compileProductionExecutableKotlinJs)
        }
    }
}