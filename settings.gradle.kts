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
include("client")
include("repository")
include("repository:compound")
include("repository:memory")
include("repository:validation")
include("repository:mongo")
include("repository:dynamo")
include("sdk")
include("server")
include("server:server_action")
include("action")
include("data-load-wrapper")
include("test-action")
include("logging")
include("export")
include("import")
include("stub-model")
include("test-logging")
