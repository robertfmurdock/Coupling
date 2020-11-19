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
        getByName("commonMain") {
            dependencies {
                implementation(project(":logging"))
                implementation("com.zegreatrob.testmints:standard:3.1.25")
                implementation("com.zegreatrob.testmints:report:3.1.25")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("io.github.microutils:kotlin-logging:2.0.3")
                implementation("com.soywiz.korlibs.klock:klock:1.12.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.github.microutils:kotlin-logging:2.0.3")
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
                implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
    }
}

tasks {
}
