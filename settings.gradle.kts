plugins {
    `gradle-enterprise`
}

rootProject.name = "Coupling"

includeBuild("plugins")
include("client")
include("server")
include("sdk")
include("e2e")
include("deploy:prerelease")
include("deploy:sandbox")
include("deploy:prod")

include("coupling-libraries:logging")
include("coupling-libraries:model")
include("coupling-libraries:stub-model")
include("coupling-libraries:test-logging")
include("coupling-libraries:action")
include("coupling-libraries:test-action")
include("coupling-libraries:json")
include("coupling-libraries:repository-core")
include("coupling-libraries:repository-compound")
include("repository-dynamo")
include("coupling-libraries:repository-memory")
include("coupling-libraries:repository-validation")
include("coupling-libraries:server_action")
include("coupling-libraries:repository-dynamo")
include("coupling-libraries:export")
include("coupling-libraries:import")

val isCiServer = System.getenv().containsKey("CI")

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}

buildCache {
    local {
        isEnabled = !isCiServer
    }
}
