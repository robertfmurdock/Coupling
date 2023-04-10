pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    `gradle-enterprise`
}

rootProject.name = "Coupling"

includeBuild("coupling-plugins")
include("client")
include("client:components")
include("server")
include("server-base")
include("sdk")
include("e2e")
include("deploy:prerelease")
include("deploy:sandbox")
include("deploy:prod")

include("coupling-libraries:action")
include("coupling-libraries:cdnLookup")
include("coupling-libraries:dependency-bom")
include("coupling-libraries:dynamo")
include("coupling-libraries:export")
include("coupling-libraries:import")
include("coupling-libraries:json")
include("coupling-libraries:logging")
include("coupling-libraries:model")
include("coupling-libraries:repository-compound")
include("coupling-libraries:repository-core")
include("coupling-libraries:repository-memory")
include("coupling-libraries:repository-validation")
include("coupling-libraries:server_action")
include("coupling-libraries:stub-model")
include("coupling-libraries:test-action")
include("coupling-libraries:test-logging")
include("coupling-libraries:test-react")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

val isCiServer = System.getenv().containsKey("CI")

buildCache {
    local {
        isEnabled = true
        removeUnusedEntriesAfterDays = 3
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
