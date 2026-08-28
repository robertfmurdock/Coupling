import com.zegreatrob.coupling.plugins.dashboard.DashboardApplicationParametersTask
import com.zegreatrob.coupling.plugins.dashboard.setup
import com.zegreatrob.coupling.plugins.js.NodeExec
import com.zegreatrob.coupling.plugins.js.setup
import com.zegreatrob.coupling.plugins.dashboard.DashboardHandoffCommandTask
import com.zegreatrob.coupling.plugins.dashboard.DashboardEndpointHealthTask
import groovy.json.JsonSlurper
import org.gradle.api.tasks.Exec

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
val dashboardCredentialsParameterArnDefault = "arn:aws:ssm:us-east-1:174159267544:parameter/ze-great-team-dashboard/github-credentials"
val dashboardCredentialsParameterArn = providers
    .environmentVariable("DASHBOARD_GITHUB_CREDENTIALS_PARAMETER_ARN")
    .orElse(dashboardCredentialsParameterArnDefault)
val bootstrapWorkDirectory = rootProject.layout.projectDirectory.dir(".bootstrap-work")
val coreDeployedStackFile = bootstrapWorkDirectory.file("core-deployed-stack.json")
val githubOidcDeployedStackFile = bootstrapWorkDirectory.file("github-oidc-deployed-stack.json")
val coreBootstrapParametersFile = bootstrapWorkDirectory.file("core-bootstrap-parameters.json")
val githubOidcBootstrapParametersFile = bootstrapWorkDirectory.file("github-oidc-bootstrap-parameters.json")
val coreChangeSetCommandFile = bootstrapWorkDirectory.file("core-change-set-command.json")
val githubOidcChangeSetCommandFile = bootstrapWorkDirectory.file("github-oidc-change-set-command.json")

tasks {
    val dashboardApplicationParameters = register<DashboardApplicationParametersTask>("dashboardApplicationParameters") {
        group = "deployment"
        description = "Generates dashboard application parameters from the consumer bootstrap manifest."
        setup(project)
        bootstrapConfig.set(dashboardBootstrapManifest)
        parametersFile.set(dashboardParametersFile)
        credentialsParameterArn.set(dashboardCredentialsParameterArn)
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
    named("check") {
        dependsOn(dashboardPackage)
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

    val captureCoreBootstrapStack = register<Exec>("captureCoreBootstrapStack") {
        group = "deployment"
        description = "Captures the deployed core dashboard bootstrap stack as JSON."
        commandLine(
            "aws", "cloudformation", "describe-stacks",
            "--stack-name", "ze-great-team-dashboard-bootstrap",
            "--region", dashboardRegion,
            "--output", "json", "--no-cli-pager",
        )
        outputs.file(coreDeployedStackFile)
        outputs.upToDateWhen { false }
        doFirst {
            val output = outputs.files.singleFile
            output.parentFile.mkdirs()
            standardOutput = output.outputStream()
        }
    }
    val captureGithubOidcBootstrapStack = register<Exec>("captureGithubOidcBootstrapStack") {
        group = "deployment"
        description = "Captures the deployed GitHub OIDC dashboard bootstrap stack as JSON."
        commandLine(
            "aws", "cloudformation", "describe-stacks",
            "--stack-name", "ze-great-team-dashboard-github-bootstrap",
            "--region", dashboardRegion,
            "--output", "json", "--no-cli-pager",
        )
        outputs.file(githubOidcDeployedStackFile)
        outputs.upToDateWhen { false }
        doFirst {
            val output = outputs.files.singleFile
            output.parentFile.mkdirs()
            standardOutput = output.outputStream()
        }
    }
    val preserveCoreBootstrapParameters = register<NodeExec>("preserveCoreBootstrapParameters") {
        group = "deployment"
        description = "Preserves deployed core bootstrap parameters for administrator review."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "parameters", "--kind", "core",
            "--config", dashboardBootstrapManifest.asFile.absolutePath,
            "--deployed-stack-json", coreDeployedStackFile.asFile.absolutePath,
            "--output", coreBootstrapParametersFile.asFile.absolutePath,
        )
        inputs.file(dashboardBootstrapManifest)
        inputs.file(coreDeployedStackFile)
        outputs.file(coreBootstrapParametersFile)
        dependsOn(captureCoreBootstrapStack)
        dependsOn(":kotlinNpmInstall")
    }
    val preserveGithubOidcBootstrapParameters = register<NodeExec>("preserveGithubOidcBootstrapParameters") {
        group = "deployment"
        description = "Preserves deployed GitHub OIDC bootstrap parameters for administrator review."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "parameters", "--kind", "github-oidc",
            "--config", dashboardBootstrapManifest.asFile.absolutePath,
            "--deployed-stack-json", githubOidcDeployedStackFile.asFile.absolutePath,
            "--core-stack-json", coreDeployedStackFile.asFile.absolutePath,
            "--output", githubOidcBootstrapParametersFile.asFile.absolutePath,
        )
        inputs.files(dashboardBootstrapManifest, githubOidcDeployedStackFile, coreDeployedStackFile)
        outputs.file(githubOidcBootstrapParametersFile)
        dependsOn(captureGithubOidcBootstrapStack, captureCoreBootstrapStack)
        dependsOn(":kotlinNpmInstall")
    }
    val generateCoreBootstrapUpdateChangeSet = register<NodeExec>("generateCoreBootstrapUpdateChangeSet") {
        group = "deployment"
        description = "Prints the core bootstrap UPDATE change-set command for administrator review."
        val couplingRootDirectory = rootProject.layout.projectDirectory.asFile.absolutePath
        val couplingFilePrefix = "file://$couplingRootDirectory/"
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "change-set", "--kind", "core",
            "--config", dashboardBootstrapManifest.asFile.absolutePath,
            "--stack-name", "ze-great-team-dashboard-bootstrap",
            "--change-set-name", "repair-core", "--change-set-type", "UPDATE",
            "--parameters", coreBootstrapParametersFile.asFile.absolutePath,
        )
        inputs.files(dashboardBootstrapManifest, coreBootstrapParametersFile)
        outputFile = coreChangeSetCommandFile.asFile
        outputs.upToDateWhen { false }
        doFirst { outputs.files.singleFile.parentFile.mkdirs() }
        dependsOn(preserveCoreBootstrapParameters)
        dependsOn(":kotlinNpmInstall")
        doLast {
            @Suppress("UNCHECKED_CAST")
            val response = JsonSlurper().parse(outputs.files.singleFile) as Map<String, Any?>
            @Suppress("UNCHECKED_CAST")
            val command = response["awsCommand"] as List<String>
            logger.lifecycle("CloudShell command (core change set):")
            logger.lifecycle(command.joinToString(" ") { value ->
                val relativeValue = value.removePrefix(couplingFilePrefix)
                val normalizedValue = if (relativeValue != value) "file://$relativeValue" else value
                "'${normalizedValue.replace("'", "'\\''")}'"
            })
            logger.lifecycle("After review, dispatch and wait (core):")
            logger.lifecycle(
                "aws cloudformation execute-change-set --change-set-name repair-core --stack-name " +
                    "ze-great-team-dashboard-bootstrap --region us-east-1 --no-cli-pager && " +
                    "aws cloudformation wait stack-update-complete --stack-name " +
                    "ze-great-team-dashboard-bootstrap --region us-east-1",
            )
        }
    }
    val generateGithubOidcBootstrapUpdateChangeSet = register<NodeExec>("generateGithubOidcBootstrapUpdateChangeSet") {
        group = "deployment"
        description = "Prints the GitHub OIDC bootstrap UPDATE change-set command for administrator review."
        val couplingRootDirectory = rootProject.layout.projectDirectory.asFile.absolutePath
        val couplingFilePrefix = "file://$couplingRootDirectory/"
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "change-set", "--kind", "github-oidc",
            "--config", dashboardBootstrapManifest.asFile.absolutePath,
            "--stack-name", "ze-great-team-dashboard-github-bootstrap",
            "--change-set-name", "repair-github-oidc", "--change-set-type", "UPDATE",
            "--parameters", githubOidcBootstrapParametersFile.asFile.absolutePath,
        )
        inputs.files(dashboardBootstrapManifest, githubOidcBootstrapParametersFile)
        outputFile = githubOidcChangeSetCommandFile.asFile
        outputs.upToDateWhen { false }
        doFirst { outputs.files.singleFile.parentFile.mkdirs() }
        dependsOn(preserveGithubOidcBootstrapParameters)
        dependsOn(":kotlinNpmInstall")
        doLast {
            @Suppress("UNCHECKED_CAST")
            val response = JsonSlurper().parse(outputs.files.singleFile) as Map<String, Any?>
            @Suppress("UNCHECKED_CAST")
            val command = response["awsCommand"] as List<String>
            logger.lifecycle("CloudShell command (GitHub OIDC change set):")
            logger.lifecycle(command.joinToString(" ") { value ->
                val relativeValue = value.removePrefix(couplingFilePrefix)
                val normalizedValue = if (relativeValue != value) "file://$relativeValue" else value
                "'${normalizedValue.replace("'", "'\\''")}'"
            })
            logger.lifecycle("After review, dispatch and wait (GitHub OIDC):")
            logger.lifecycle(
                "aws cloudformation execute-change-set --change-set-name repair-github-oidc --stack-name " +
                    "ze-great-team-dashboard-github-bootstrap --region us-east-1 --no-cli-pager && " +
                    "aws cloudformation wait stack-update-complete --stack-name " +
                    "ze-great-team-dashboard-github-bootstrap --region us-east-1",
            )
        }
    }
    val dashboardBootstrapUpgrade = register<NodeExec>("dashboardBootstrapUpgrade") {
        group = "deployment"
        description = "Updates the desired-state bootstrap metadata; review and commit the manifest mutation."
        setup(project)
        nodeCommand = "ze-great-dashboard-aws"
        arguments = listOf(
            "bootstrap", "upgrade", "--config", dashboardBootstrapManifest.asFile.absolutePath,
        )
        inputs.file(dashboardBootstrapManifest)
        outputs.file(dashboardBootstrapManifest)
        outputs.upToDateWhen { false }
        dependsOn(":kotlinNpmInstall")
    }
    register("dashboardBootstrapGenerateChangeSets") {
        group = "deployment"
        description = "Captures bootstrap stacks, preserves parameters, and generates both UPDATE change sets for review."
        dependsOn(generateCoreBootstrapUpdateChangeSet, generateGithubOidcBootstrapUpdateChangeSet)
    }
    register("dashboardBootstrapRevalidate") {
        group = "verification"
        description = "Revalidates dashboard bootstrap contracts after administrator changes."
        dependsOn(dashboardBootstrapCheck)
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
