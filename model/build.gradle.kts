import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
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
                api("com.soywiz.korlibs.klock:klock:2.4.8")
                implementation("com.benasher44:uuid:0.3.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(kotlin("test", BuildConstants.kotlinVersion))
                implementation("com.zegreatrob.testmints:standard:5.3.6")
                implementation("com.zegreatrob.testmints:minassert:5.3.6")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
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
