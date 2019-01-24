import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "1.3.20"
}

repositories {
    mavenCentral()
}

kotlin {
    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.3.20")
                implementation("org.jetbrains.kotlin:kotlin-test-common:1.3.20")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.3.20")
            }
        }

        getByName("jsMain") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:1.3.20")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }
    }
}

tasks {
    getByName<Kotlin2JsCompile>("compileKotlinJs") {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
