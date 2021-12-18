pluginManagement {
    repositories {
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlinx-serialization") {
                useModule("org.jetbrains.kotlin:kotlin-serialization:${requested.version}")
            }
            if (requested.id.id == "kotlin2js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "Coupling"

includeBuild("plugins")
include("action")
include("client")
include("e2e")
include("export")
include("import")
include("json")
include("logging")
include("model")
include("repository-core")
include("repository-compound")
include("repository-dynamo")
include("repository-memory")
include("repository-mongo")
include("repository-validation")
include("sdk")
include("server")
include("server_action")
include("stub-model")
include("test-action")
include("test-logging")

