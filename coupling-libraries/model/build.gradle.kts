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
                api("com.soywiz.korlibs.klock:klock")
                api("org.jetbrains.kotlinx:kotlinx-datetime")
                implementation("com.benasher44:uuid")
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
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.junit.jupiter:junit-jupiter-engine")
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
