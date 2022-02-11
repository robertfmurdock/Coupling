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
