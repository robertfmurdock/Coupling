import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.coupling.plugins.dashboard.DashboardArtifactUploadTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardCloudFormationDeployTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardEndpointHealthTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardGatewayParametersTask

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
val dashboardGatewayStackName = "ze-great-team-dashboard-gateway"
val boardConfig = layout.projectDirectory.file("board.yaml")
val dashboardBootstrapManifest = layout.projectDirectory.file("bootstrap.json")
val dashboardParametersFile = layout.buildDirectory.file("dashboard/application-parameters.json")
val dashboardGatewayParametersFile = layout.buildDirectory.file("dashboard/gateway-parameters.json")
val dashboardGatewayTemplate = layout.projectDirectory.file("gateway.yml")
val releaseDirectory = layout.buildDirectory.dir("release")
val dashboardDryRun = providers.gradleProperty("dashboardDryRun").map(String::toBoolean).orElse(false)
val dashboardExecutionRoleArn = providers.environmentVariable("DASHBOARD_CLOUDFORMATION_EXECUTION_ROLE_ARN")

tasks {
    register<NodeExec>("dashboardPackage") {
        group = "deployment"
        description = "Packages the Ze Great Team dashboard Lambda release."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "package",
            "--board-config", boardConfig.asFile.absolutePath,
            "--output", releaseDirectory.get().asFile.absolutePath,
        )
        inputs.file(boardConfig)
        outputs.dir(releaseDirectory)
        dependsOn(":kotlinNpmInstall")
    }

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
        dependsOn(":kotlinNpmInstall")
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

    val dashboardPackage = named("dashboardPackage")
    val dashboardUploadArtifact = register<DashboardArtifactUploadTask>("dashboardUploadArtifact") {
        group = "deployment"
        description = "Uploads the packaged Lambda artifact to the dashboard artifact bucket."
        parametersFile.set(dashboardParametersFile)
        releaseFile.set(releaseDirectory.map { it.file("release.json") })
        lambdaZip.set(releaseDirectory.map { it.file("lambda.zip") })
        region.set(dashboardRegion)
        dryRun.set(dashboardDryRun)
        dependsOn(dashboardPackage)
    }
    val dashboardDeployStack = register<DashboardCloudFormationDeployTask>("dashboardDeployStack") {
        group = "deployment"
        description = "Deploys the dashboard CloudFormation stack, or validates its template with -PdashboardDryRun=true."
        parametersFile.set(dashboardParametersFile)
        templateFile.set(releaseDirectory.map { it.file("template.yml") })
        region.set(dashboardRegion)
        stackName.set(dashboardStackName)
        dryRun.set(dashboardDryRun)
        executionRoleArn.set(dashboardExecutionRoleArn)
        dependsOn(dashboardDoctor, dashboardUploadArtifact)
    }
    val dashboardGatewayParameters = register<DashboardGatewayParametersTask>("dashboardGatewayParameters") {
        group = "deployment"
        description = "Generates public gateway parameters from the consumer bootstrap manifest."
        bootstrapManifest.set(dashboardBootstrapManifest)
        parametersFile.set(dashboardGatewayParametersFile)
    }
    val dashboardGatewayDeployStack = register<DashboardCloudFormationDeployTask>("dashboardGatewayDeployStack") {
        group = "deployment"
        description = "Deploys the Coupling-owned public dashboard gateway."
        parametersFile.set(dashboardGatewayParametersFile)
        templateFile.set(dashboardGatewayTemplate)
        region.set(dashboardRegion)
        stackName.set(dashboardGatewayStackName)
        dryRun.set(dashboardDryRun)
        dependsOn(dashboardGatewayParameters)
    }
    register<DashboardEndpointHealthTask>("dashboardGatewayHealthCheck") {
        group = "verification"
        description = "Requires successful health and dashboard responses through the public gateway."
        region.set(dashboardRegion)
        stackName.set(dashboardGatewayStackName)
        endpointOutputKey.set("ApiEndpoint")
        endpointFile.set(layout.buildDirectory.file("release/gateway-url.txt"))
        dependsOn(dashboardGatewayDeployStack)
    }
}
