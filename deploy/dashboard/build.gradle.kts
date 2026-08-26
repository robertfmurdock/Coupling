import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.coupling.plugins.dashboard.DashboardEndpointHealthTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardHandoffCommandTask
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

plugins {
    id("com.zegreatrob.coupling.plugins.jstools")
}

kotlin {
    js {
        nodejs()
    }
}

dependencies {
    jsMainImplementation(npmConstrained("@continuous-excellence/ze-great-dashboard-aws"))
}

val dashboardRegion = "us-east-1"
val dashboardStackName = "ze-great-team-dashboard"
val boardConfig = layout.projectDirectory.file("board.yaml")
val dashboardBootstrapManifest = layout.projectDirectory.file("bootstrap.json")
val dashboardParametersFile = layout.buildDirectory.file("dashboard/application-parameters.json")
val releaseDirectory = layout.buildDirectory.dir("release")
val dashboardDryRun = providers.gradleProperty("dashboardDryRun").map(String::toBoolean).orElse(false)
val dashboardExecutionRoleArn = providers.environmentVariable("AWS_CLOUDFORMATION_EXECUTION_ROLE_ARN")
val dashboardCredentialsParameterArn = providers
    .environmentVariable("DASHBOARD_GITHUB_CREDENTIALS_PARAMETER_ARN")
    .orElse("")

tasks {
    val dashboardApplicationParameters = register<NodeExec>("dashboardApplicationParameters") {
        group = "deployment"
        description = "Generates dashboard application parameters from the consumer bootstrap manifest."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "parameters",
            "--bootstrap-config", dashboardBootstrapManifest.asFile.absolutePath,
            "--output", dashboardParametersFile.get().asFile.absolutePath,
        )
        inputs.file(dashboardBootstrapManifest)
        outputs.file(dashboardParametersFile)
        inputs.property("dashboardCredentialsParameterArn", dashboardCredentialsParameterArn)
        doLast {
            val parametersFile = dashboardParametersFile.get().asFile
            @Suppress("UNCHECKED_CAST")
            val parameters = JsonSlurper().parse(parametersFile) as MutableList<MutableMap<String, Any?>>
            parameters.removeIf { it["ParameterKey"] == "SecretReference" }
            dashboardCredentialsParameterArn.orNull
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?.let { arn ->
                    parameters += mutableMapOf(
                        "ParameterKey" to "SecretReference",
                        "ParameterValue" to arn,
                    )
                }
            parametersFile.writeText("${JsonOutput.prettyPrint(JsonOutput.toJson(parameters))}\n")
        }
        dependsOn(":kotlinNpmInstall")
    }
    val dashboardPackage = register<NodeExec>("dashboardPackage") {
        group = "deployment"
        description = "Packages the Ze Great Team dashboard Lambda release."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "package",
            "--board-config", boardConfig.asFile.absolutePath,
            "--parameters", dashboardParametersFile.get().asFile.absolutePath,
            "--output", releaseDirectory.get().asFile.absolutePath,
        )
        inputs.files(boardConfig, dashboardParametersFile)
        outputs.dir(releaseDirectory)
        dependsOn(dashboardApplicationParameters)
    }

    val dashboardDoctor = register<NodeExec>("dashboardDoctor") {
        group = "verification"
        description = "Checks dashboard deployment prerequisites without modifying AWS."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf("doctor", "--parameters", dashboardParametersFile.get().asFile.absolutePath, "--region", dashboardRegion)
        inputs.file(dashboardParametersFile)
        dependsOn(dashboardApplicationParameters)
    }

    val dashboardBootstrapCheck = register<NodeExec>("dashboardBootstrapCheck") {
        group = "verification"
        description = "Checks the deployed dashboard bootstrap contracts without modifying AWS."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "check",
            "--config", dashboardBootstrapManifest.asFile.absolutePath,
        )
        inputs.file(dashboardBootstrapManifest)
        dependsOn(":kotlinNpmInstall")
    }

    val dashboardUploadArtifact = register<DashboardHandoffCommandTask>("dashboardUploadArtifact") {
        group = "deployment"
        description = "Executes the generated dashboard artifact upload handoff."
        deploymentFile.set(releaseDirectory.map { it.file("deployment.json") })
        commandName.set("upload")
        runtimeValues.put("AWS_REGION", dashboardRegion)
        dryRun.set(dashboardDryRun)
        dependsOn(dashboardPackage)
    }
    val dashboardDeployStack = register<DashboardHandoffCommandTask>("dashboardDeployStack") {
        group = "deployment"
        description = "Executes the generated dashboard CloudFormation handoff, or validates its template with -PdashboardDryRun=true."
        deploymentFile.set(releaseDirectory.map { it.file("deployment.json") })
        commandName.set("deploy")
        runtimeValues.put("AWS_REGION", dashboardRegion)
        runtimeValues.put("STACK_NAME", dashboardStackName)
        runtimeValues.put("AWS_CLOUDFORMATION_EXECUTION_ROLE_ARN", dashboardExecutionRoleArn)
        dryRun.set(dashboardDryRun)
        dependsOn(dashboardBootstrapCheck, dashboardDoctor, dashboardUploadArtifact)
    }
    val dashboardGatewayHealthCheck = register<DashboardEndpointHealthTask>("dashboardGatewayHealthCheck") {
        group = "verification"
        description = "Requires successful health and dashboard responses through the existing public gateway."
        bootstrapManifest.set(dashboardBootstrapManifest)
        region.set(dashboardRegion)
        endpointFile.set(layout.buildDirectory.file("release/gateway-url.txt"))
        dryRun.set(dashboardDryRun)
        dependsOn(dashboardDeployStack)
    }
    register("dashboardDeploy") {
        group = "deployment"
        description = "Deploys the dashboard application and verifies it through the existing public gateway."
        dependsOn(dashboardGatewayHealthCheck)
    }
}
