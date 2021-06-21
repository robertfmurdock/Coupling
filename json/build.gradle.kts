
import com.zegreatrob.coupling.build.BuildConstants

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        jvm()
        js {
            useCommonJs()
            nodejs()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(kotlin("stdlib", BuildConstants.kotlinVersion))
                api(kotlin("stdlib-common", BuildConstants.kotlinVersion))
                api("com.soywiz.korlibs.klock:klock:2.1.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation("com.zegreatrob.testmints:standard:4.0.21")
                implementation("com.zegreatrob.testmints:minassert:4.0.21")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
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
                implementation(kotlin("stdlib-js", BuildConstants.kotlinVersion))
            }
        }
    }
}

tasks {
}
