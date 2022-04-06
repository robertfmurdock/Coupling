plugins {
    `gradle-enterprise`
}

rootProject.name = "Coupling"

includeBuild("plugins")
includeBuild("coupling-libraries")
include("client")
include("server")
include("sdk")
include("e2e")
include("deploy:prerelease")
include("deploy:sandbox")
include("deploy:prod")

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
