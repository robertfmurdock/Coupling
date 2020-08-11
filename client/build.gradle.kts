import com.moowork.gradle.node.yarn.YarnTask
import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.loadPackageJson

plugins {
    kotlin("js")
    id("com.github.node-gradle.node")
}

node {
    version = BuildConstants.nodeVersion
    npmVersion = BuildConstants.npmVersion
    yarnVersion = BuildConstants.yarnVersion
    download = true
}

kotlin {
    target {
        useCommonJs()
        browser {
            testTask {
                enabled = true
            }
        }
    }

    sourceSets {
        val main by getting {
            resources.srcDir("src/main/javascript")
        }
    }
}

val packageJson = loadPackageJson()

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation(project(":model"))
    implementation(project(":json"))
    implementation(project(":sdk"))
    implementation(project(":action"))
    implementation(project(":logging"))
    implementation(project(":repository:memory"))
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }
    implementation("com.zegreatrob.testmints:minreact:+")
    implementation("com.zegreatrob.testmints:react-data-loader:+")
    implementation("com.zegreatrob.testmints:action:+")
    implementation("com.zegreatrob.testmints:action-async:+")
    implementation("com.soywiz.korlibs.klock:klock:1.10.6")
    implementation("com.benasher44:uuid:0.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.20.0-1.3.70-eap-274-2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.3.72")
    implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.107-kotlin-1.3.72")

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    testImplementation("com.zegreatrob.testmints:minenzyme:+")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:+")
    testImplementation("com.zegreatrob.testmints:async:+")
    testImplementation("com.zegreatrob.testmints:minassert:+")
    testImplementation("com.zegreatrob.testmints:minspy:+")

}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {

    val updateDependencies by creating(YarnTask::class) {
        dependsOn(yarn)
        args = listOf("run", "ncu", "-u")
    }

}
