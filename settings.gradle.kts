rootProject.name = "Coupling"

includeBuild("plugins")
includeBuild("coupling-libraries")
include("client")
include("e2e")
include("export")
include("import")
include("repository-dynamo")
include("sdk")
include("server")
include("server_action")

