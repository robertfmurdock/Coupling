pluginManagement {
    repositories {
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
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
include("repository")
include("sdk")
include("server")
include("server:server_action")
include("action")
include("logging")
include("stub-model")
include("test-logging")
