import com.zegreatrob.coupling.plugins.BuildConstants

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}
group = "com.zegreatrob.coupling.libraries"
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
                api("com.soywiz.korlibs.klock:klock:2.4.13")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation("com.benasher44:uuid:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(kotlin("test", BuildConstants.kotlinVersion))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
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
