pluginManagement {
    repositories {
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
        }
    }
}

rootProject.name = "Coupling"
include("model")
include("json")
include("mongo")
include("client")
include("server")
include("engine")
include("commonKt")
include("logging")
include("test-logging")

enableFeaturePreview("GRADLE_METADATA")