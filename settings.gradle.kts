pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("com.gradle.develocity") version "4.1.1"
}

rootProject.name = "Coupling"

includeBuild("coupling-plugins")

include("cli")
include("client")
include("client:components")
include("client:components:isolated")
include("client:components:graphing")
include("client:components:external")
include("deploy:prerelease")
include("deploy:prod")
include("deploy:sandbox")
include("e2e")
include("konsist")
include("libraries:action")
include("libraries:auth0-management")
include("libraries:dependency-bom")
include("libraries:js-dependencies")
include("libraries:json")
include("libraries:logging")
include("libraries:model")
include("libraries:repository:compound")
include("libraries:repository:core")
include("libraries:repository:dynamo")
include("libraries:repository:memory")
include("libraries:repository:validation")
include("libraries:stub-model")
include("libraries:test-action")
include("libraries:test-logging")
include("libraries:test-react")
include("scripts:cdn-lookup")
include("scripts:export")
include("scripts:import")
include("sdk")
include("server")
include("server:actionz")
include("server:cache-repository")
include("server:base")
include("server:discord")
include("server:secret")
include("server:slack")

develocity {
    buildScan {
        publishing.onlyIf { System.getenv().containsKey("CI") }
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"
        tag("CI")
    }
}

buildCache {
    local {
        isEnabled = true
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
