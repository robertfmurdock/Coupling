plugins {
    `gradle-enterprise`
}

rootProject.name = "Coupling"

includeBuild("plugins")
include("client")
include("server")
include("server-base")
include("sdk")
include("e2e")
include("deploy:prerelease")
include("deploy:sandbox")
include("deploy:prod")

include("dynamo")
include("coupling-libraries:logging")
include("coupling-libraries:model")
include("coupling-libraries:stub-model")
include("coupling-libraries:test-logging")
include("coupling-libraries:action")
include("coupling-libraries:test-action")
include("coupling-libraries:json")
include("coupling-libraries:repository-core")
include("coupling-libraries:repository-compound")
include("coupling-libraries:repository-memory")
include("coupling-libraries:repository-validation")
include("coupling-libraries:server_action")
include("coupling-libraries:cdnLookup")
include("coupling-libraries:export")
include("coupling-libraries:import")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
