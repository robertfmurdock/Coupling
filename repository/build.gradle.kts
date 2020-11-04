
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
                api(project(":model"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:standard:3.1.21")
                implementation("com.zegreatrob.testmints:minassert:3.1.21")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect", BuildConstants.kotlinVersion))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")

                implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }
}

tasks {
}
