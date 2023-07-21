plugins {
    id("com.zegreatrob.coupling.plugins.mp")
}

kotlin {
    targets {
        js {
            nodejs { testTask(Action { useMocha { timeout = "10s" } }) }

            val main = compilations.findByName("main")!!
            val test = compilations.findByName("test")!!
            test.defaultSourceSet.dependsOn(main.defaultSourceSet)
        }
        jvm()
    }

    sourceSets {
        named("jsTest") {
            kotlin {
                srcDir(projectDir.resolve("src/commonTest/kotlin"))
            }
        }
    }
}

dependencies {
    "commonMainApi"(project(":libraries:action"))
    "commonMainApi"(project(":libraries:model"))
    "commonMainImplementation"(project(":libraries:json"))
    "commonMainImplementation"(project(":libraries:repository:core"))
    "commonMainImplementation"("io.ktor:ktor-client-content-negotiation")
    "commonMainImplementation"("io.ktor:ktor-client-core")
    "commonMainImplementation"("io.ktor:ktor-client-logging")
    "commonMainImplementation"("io.ktor:ktor-client-websockets")
    "commonMainImplementation"("io.ktor:ktor-serialization-kotlinx-json")
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib")
    "commonMainImplementation"("org.jetbrains.kotlin:kotlin-stdlib-common")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-serialization-json")

    "commonTestImplementation"(project(":libraries:repository:validation"))
    "commonTestImplementation"(project(":libraries:stub-model"))
    "commonTestImplementation"(project(":libraries:test-logging"))
    "commonTestImplementation"("com.benasher44:uuid")
    "commonTestImplementation"("com.zegreatrob.testmints:async")
    "commonTestImplementation"("com.zegreatrob.testmints:minassert")
    "commonTestImplementation"("com.zegreatrob.testmints:standard")
    "commonTestImplementation"("io.github.oshai:kotlin-logging")
    "commonTestImplementation"("org.jetbrains.kotlin:kotlin-test")

    "jsMainImplementation"("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    "jsTestImplementation"(project(":server:slack"))
    "jvmTestImplementation"("io.ktor:ktor-client-java")
}

tasks {
    val jsNodeTest by getting {
        dependsOn(":composeUp")
        outputs.cacheIf { true }
    }
    "jvmTest" {
        dependsOn(":composeUp", jsNodeTest)
        dependsOn(":importCert")
    }
}
