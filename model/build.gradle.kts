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
                api("com.soywiz.korlibs.klock:klock:2.0.6")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(kotlin("test", BuildConstants.kotlinVersion))
                implementation(kotlin("test-common", BuildConstants.kotlinVersion))
                implementation(kotlin("test-annotations-common", BuildConstants.kotlinVersion))
                implementation("com.zegreatrob.testmints:standard:3.2.11")
                implementation("com.zegreatrob.testmints:minassert:3.2.4")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5", BuildConstants.kotlinVersion))
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
