plugins {
    id("com.zegreatrob.coupling.plugins.mp")
    id("com.zegreatrob.testmints.action-mint")
    kotlin("plugin.serialization")
}

kotlin {
    targets {
        js {
            nodejs { testTask(Action { useMocha { timeout = "10s" } }) }
        }
        jvm()
    }

    sourceSets.named("jsMain") {
        kotlin.srcDir("build/generated/ksp/js/jsMain/kotlin")
    }
    sourceSets.named("jvmMain") {
        kotlin.srcDir("build/generated/ksp/jvm/jvmMain/kotlin")
    }
}

dependencies {
    commonMainImplementation(project(":libraries:model"))
    commonMainImplementation(project(":libraries:logging"))
    commonMainApi("com.zegreatrob.testmints:action")
    commonMainApi("com.zegreatrob.testmints:action-async")
    commonMainImplementation("org.jetbrains.kotlin:kotlin-stdlib-common")
    commonMainImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    commonMainImplementation("com.benasher44:uuid")
    commonMainImplementation("io.github.microutils:kotlin-logging")

    commonTestImplementation(project(":libraries:json"))
    commonTestImplementation(project(":libraries:stub-model"))
    commonTestImplementation(project(":libraries:test-logging"))
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-core")
    commonTestImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
    commonTestImplementation("org.jetbrains.kotlin:kotlin-test")
    commonTestImplementation("com.zegreatrob.testmints:standard")
    commonTestImplementation("com.zegreatrob.testmints:async")
    commonTestImplementation("com.zegreatrob.testmints:minassert")

    "jvmMainImplementation"(kotlin("reflect"))
    "jvmMainImplementation"("io.github.microutils:kotlin-logging")
    "jvmMainImplementation"("com.fasterxml.jackson.core:jackson-databind")

    "jvmTestImplementation"(kotlin("reflect"))
    "jvmTestImplementation"("org.slf4j:slf4j-simple")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "jvmTestImplementation"("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    named<Test>("jvmTest") {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
        useJUnitPlatform()
    }
    "formatKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    "formatKotlinJsTest" {
        dependsOn("kspTestKotlinJs")
    }
    "lintKotlinJsMain" {
        dependsOn("kspKotlinJs")
    }
    "lintKotlinJsTest" {
        dependsOn("kspTestKotlinJs")
    }
}
