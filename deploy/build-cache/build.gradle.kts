import com.zegreatrob.coupling.plugins.deploy.BuildCacheDeploymentTask

val buildCacheRegion = "us-east-1"
val buildCacheStackName = "coupling-gradle-build-cache"
val buildCacheTemplate = layout.projectDirectory.file("template.yaml")
val buildCacheDryRun = providers.gradleProperty("buildCacheDryRun").map(String::toBoolean).orElse(false)

tasks.register<BuildCacheDeploymentTask>("buildCacheDeploy") {
    group = "deployment"
    description = "Deploys the Gradle build-cache bucket and CI role policies, or validates its template with -PbuildCacheDryRun=true."
    template.set(buildCacheTemplate)
    stackName.set(buildCacheStackName)
    region.set(buildCacheRegion)
    deploymentRoleName.set("CouplingDeploy")
    pullRequestRoleName.set("LocalDevelopment")
    dryRun.set(buildCacheDryRun)
}
