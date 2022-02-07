rootProject.name = "coupling-libraries"

include("logging")
include("model")
include("stub-model")
include("test-logging")
include("action")
include("test-action")
include("json")
include("repository-core")
include("repository-compound")
include("repository-memory")
include("repository-validation")
includeBuild("../plugins")