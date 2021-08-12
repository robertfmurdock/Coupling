import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib", BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("com.benasher44:uuid:0.3.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(kotlin("test", BuildConstants.kotlinVersion))
                implementation("com.zegreatrob.testmints:standard:4.1.10")
                implementation("com.zegreatrob.testmints:minassert:4.1.10")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.0-M1")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
            }
        }

        val jsMain by getting {
            dependencies {
                api(kotlin("stdlib-js", BuildConstants.kotlinVersion))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js", BuildConstants.kotlinVersion))
            }
        }
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

tasks {
}
