plugins {
    id("com.zegreatrob.coupling.plugins.mp")
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
                api(kotlin("stdlib"))
                api(kotlin("stdlib-common"))
                api("com.soywiz.korlibs.klock:klock:3.0.0")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
                implementation("com.benasher44:uuid:0.5.0")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(project(":coupling-libraries:test-logging"))
                implementation(kotlin("test"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
            }
        }
        getByName("jsMain") {
            dependencies {
                api(kotlin("stdlib-js"))
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
