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
include("action")
include("client")
include("e2e")
include("export")
include("import")
include("json")
include("logging")
include("model")
include("repository")
include("repository:compound")
include("repository:memory")
include("repository:validation")
include("repository:mongo")
include("repository:dynamo")
include("sdk")
include("server")
include("server:server_action")
include("stub-model")
include("test-action")
include("test-logging")
include("wdio")
