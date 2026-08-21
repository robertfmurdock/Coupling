import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.coupling.plugins.dashboard.DashboardArtifactUploadTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardCloudFormationDeployTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardEndpointHealthTask

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
val dashboardParametersFile = layout.projectDirectory.file("parameters.json")
val releaseDirectory = layout.buildDirectory.dir("release")
val dashboardDryRun = providers.gradleProperty("dashboardDryRun").map(String::toBoolean).orElse(false)

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

    val dashboardDoctor = register<NodeExec>("dashboardDoctor") {
        group = "verification"
        description = "Checks dashboard deployment prerequisites without modifying AWS."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf("doctor", "--parameters", dashboardParametersFile.asFile.absolutePath, "--region", dashboardRegion)
        inputs.file(dashboardParametersFile)
        dependsOn(":kotlinNpmInstall")
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
        dependsOn(dashboardUploadArtifact)
    }
    register<DashboardEndpointHealthTask>("dashboardHealthCheck") {
        group = "verification"
        description = "Requires successful health and dashboard responses from the deployed Function URL."
        region.set(dashboardRegion)
        stackName.set(dashboardStackName)
        endpointFile.set(layout.buildDirectory.file("release/function-url.txt"))
        dependsOn(dashboardDoctor, dashboardDeployStack)
    }
}
