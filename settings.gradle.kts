pluginManagement {
    repositories {
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
        // mavenLocal()
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
include("client")
include("server")
include("engine")
include("commonKt")
include("test-style")

enableFeaturePreview("GRADLE_METADATA")