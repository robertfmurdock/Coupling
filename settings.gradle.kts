rootProject.name = "Coupling"

includeBuild("plugins")
includeBuild("coupling-libraries")
include("client")
include("e2e")
include("export")
include("import")
include("repository-core")
include("repository-compound")
include("repository-dynamo")
include("repository-memory")
include("repository-validation")
include("sdk")
include("server")
include("server_action")

