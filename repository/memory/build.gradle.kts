
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.BuildConstants.testmintsVersion

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
                api(project(":repository"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
                api("com.benasher44:uuid:0.2.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(project(":repository:validation"))
                implementation("com.zegreatrob.testmints:standard:$testmintsVersion")
                implementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect", BuildConstants.kotlinVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")

                implementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
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
                implementation("com.zegreatrob.testmints:async:$testmintsVersion")
            }
        }
    }
}

tasks {
}
