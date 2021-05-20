
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.zegreatrob.coupling.build.JsonLoggingTestListener
import de.gliderpilot.gradle.semanticrelease.SemanticReleaseChangeLogService
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.grgit.Commit
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    id("com.github.node-gradle.node") apply false
    id("com.bmuschko.docker-remote-api") version "7.0.0"
    id("se.patrikerdes.use-latest-versions") version "0.2.16"
    id("com.github.ben-manes.versions") version "0.38.0"
    id("net.rdrei.android.buildtimetracker") version "0.11.0"
    id("de.gliderpilot.semantic-release") version "1.4.2"
}

semanticRelease {
    changeLog(closureOf<SemanticReleaseChangeLogService> {
        changeScope = KotlinClosure1<Commit, ChangeScope>({ ChangeScope.PATCH })
    })
}

allprojects {
    apply(plugin = "se.patrikerdes.use-latest-versions")
    apply(plugin = "com.github.ben-manes.versions")    
    repositories {
        mavenCentral()
        maven { url = uri("https://kotlin.bintray.com/kotlinx") }
        maven { url = uri("https://kotlin.bintray.com/kotlin-js-wrappers") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers") }
    }

    tasks {
        val copyReportsToCircleCIDirectory by creating(Copy::class) {
            from("build/reports")
            into("${rootProject.buildDir.path}/test-output/${project.path}/reports")
        }
        val copyTestResultsToCircleCIDirectory by creating(Copy::class) {
            from("build/test-results")
            into("${rootProject.buildDir.path}/test-output/${project.path}/results")
        }

        val collectResults by creating {
            dependsOn(copyReportsToCircleCIDirectory, copyTestResultsToCircleCIDirectory)
        }

        withType<DependencyUpdatesTask> {
            checkForGradleUpdate = true
            outputFormatter = "json"
            outputDir = "build/dependencyUpdates"
            reportfileName = "report"
            revision = "release"
            rejectVersionIf {
                "^[0-9.]+-M[0-9](-r)?\$"
                    .toRegex()
                    .matches(candidate.version)
            }
        }
    }

    afterEvaluate {
        mkdir(file(rootProject.buildDir.toPath().resolve("test-output")))
        tasks.withType(KotlinJsTest::class) {
            addTestListener(JsonLoggingTestListener(path))
        }
    }
}

docker {
    registryCredentials {
        username.set(System.getenv("DOCKER_USER"))
        password.set(System.getenv("DOCKER_PASS"))
        email.set(System.getenv("DOCKER_EMAIL"))
    }
}

tasks {
    val pullProductionImage by creating(DockerPullImage::class) {
        image.set("zegreatrob/coupling:latest")
    }

    val buildProductionImage by creating(DockerBuildImage::class) {
        mustRunAfter("pullProductionImage")
        inputDir.set(file("./"))
        dockerFile.set(file("Dockerfile.prod"))
        remove.set(false)
        images.add("zegreatrob/coupling:latest")
        images.add("zegreatrob/coupling:${version}")
    }

    val pushProductionImage by creating(DockerPushImage::class) {
        mustRunAfter("buildProductionImage")
        images.add("zegreatrob/coupling:latest")
    }
}

buildtimetracker {
    reporters {
        register("csv") {
            options.run {
                put("output", "build/times.csv")
                put("append", "true")
                put("header", "false")
            }
        }

        register("summary") {
            options.run {
                put("ordered", "false")
                put("threshold", "50")
                put("header", "false")
            }
        }

        register("csvSummary") {
            options.run {
                put("csv", "build/times.csv")
            }
        }
    }
}
