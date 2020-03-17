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
include("dynamo")
include("client")
include("repository")
include("repository:compound")
include("repository:memory")
include("repository:validation")
include("sdk")
include("server")
include("server:server_action")
include("action")
include("logging")
include("export")
include("stub-model")
include("test-logging")
