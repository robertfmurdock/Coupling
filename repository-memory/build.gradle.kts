
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
                implementation(project(":model"))
                implementation(project(":repository-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
                implementation("com.benasher44:uuid:0.3.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(project(":repository-validation"))
                implementation("com.zegreatrob.testmints:standard:4.1.2")
                implementation("com.zegreatrob.testmints:minassert:4.1.2")
                implementation("org.jetbrains.kotlin:kotlin-test")
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
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("com.zegreatrob.testmints:async:4.1.10")
            }
        }
    }
}

tasks {
}
