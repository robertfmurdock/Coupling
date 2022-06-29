import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsExec

plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.coupling.plugins.serialization")
}

group = "com.zegreatrob.coupling.libraries"

kotlin {
    targets {
        js {
            nodejs {
                binaries.executable()
                testTask { useMocha { timeout = "400s" } }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.0.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
                implementation("io.ktor:ktor-client-content-negotiation:2.0.2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.0.2")
                implementation("io.ktor:ktor-client-logging:2.0.3")
                implementation("io.ktor:ktor-client-encoding:2.0.2")
                implementation(npm("resolve-pkg", "^1.0.0"))
                implementation(npm("read-pkg-up", "^4.0.0"))
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(npm("react", "18.1.0"))
                implementation("com.zegreatrob.testmints:standard")
                implementation("com.zegreatrob.testmints:async")
                implementation("com.zegreatrob.testmints:minassert")
            }
        }
    }
}

val outputFile: String? by project

tasks {
    named("jsNodeRun", NodeJsExec::class) {
        this.args("react")
        outputFile?.let {
            standardOutput = file("${System.getProperty("user.dir")}/$it").outputStream()
        }
    }
}
