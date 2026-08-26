pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("com.gradle.develocity") version "4.5.0"
    id("com.github.burrunan.s3-build-cache") version "1.9.8"
}

rootProject.name = "Coupling"

includeBuild("coupling-plugins")

include("cli")
include("cli:test-log-tools")
include("client")
include("client:components")
include("client:components:graphing")
include("client:components:external")
include("deploy:prerelease")
include("deploy:prod")
include("deploy:sandbox")
include("deploy:dashboard")
include("deploy:build-cache")
include("e2e")
include("konsist")
include("libraries:action")
include("libraries:auth0-management")
include("libraries:dependency-bom")
include("libraries:js-dependencies")
include("libraries:json")
include("libraries:kotlin-react-router-dom-legacy")
include("libraries:logging")
include("libraries:model")
include("libraries:repository:compound")
include("libraries:repository:core")
include("libraries:repository:dynamo")
include("libraries:repository:memory")
include("libraries:repository:validation")
include("libraries:stub-model")
include("libraries:test-log-analysis")
include("libraries:test-action")
include("libraries:test-logging")
include("libraries:test-react")
include("scripts:cdn-lookup")
include("scripts:import")
include("scripts:weekly-cleanup")
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
    val buildCacheBucket = System.getenv("GRADLE_BUILD_CACHE_BUCKET")
    if (System.getenv().containsKey("CI") && !buildCacheBucket.isNullOrBlank()) {
        remote<com.github.burrunan.s3cache.AwsS3BuildCache> {
            region = System.getenv("GRADLE_BUILD_CACHE_REGION") ?: "us-east-1"
            bucket = buildCacheBucket
            prefix = "gradle-build-cache/"
            lookupDefaultAwsCredentials = true
            isPush = System.getenv("GITHUB_REF") == "refs/heads/master"
        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
