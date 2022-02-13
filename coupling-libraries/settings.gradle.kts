plugins {
    id("com.gradle.enterprise") version "3.8.1"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

rootProject.name = "coupling-libraries"

include("logging")
include("model")
include("stub-model")
include("test-logging")
include("action")
include("test-action")
include("json")
include("repository-core")
include("repository-compound")
include("repository-memory")
include("repository-validation")
include("server_action")
include("repository-dynamo")
include("export")
include("import")

includeBuild("../plugins")
