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
include("libraries:action")
include("libraries:cdnLookup")
include("libraries:dependency-bom")
include("libraries:dynamo")
include("libraries:export")
include("libraries:import")
include("libraries:js-dependencies")
include("libraries:json")
include("libraries:logging")
include("libraries:model")
include("libraries:repository:compound")
include("libraries:repository:core")
include("libraries:repository:memory")
include("libraries:repository:validation")
include("libraries:stub-model")
include("libraries:test-action")
include("libraries:test-logging")
include("libraries:test-react")
include("deploy:prerelease")
include("deploy:prod")
include("deploy:sandbox")
include("e2e")
include("sdk")
include("server")
include("server:action")
include("server:base")

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
