import com.zegreatrob.coupling.build.loadPackageJson
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.4.10"
}

kotlin {
    js {
        useCommonJs()
        browser()
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
    implementation("com.zegreatrob.testmints:minreact:3.1.17")
    implementation("com.zegreatrob.testmints:react-data-loader:3.1.17")
    implementation("com.zegreatrob.testmints:action:3.1.18")
    implementation("com.zegreatrob.testmints:action-async:3.1.18")
    implementation("com.soywiz.korlibs.klock:klock:1.12.0")
    implementation("com.benasher44:uuid:0.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.111-kotlin-1.4.10")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.121-kotlin-1.4.10")
    implementation("org.jetbrains:kotlin-styled:5.2.0-pre.117-kotlin-1.4.10")
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.111-kotlin-1.4.0")

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    testImplementation("com.zegreatrob.testmints:minenzyme:3.1.17")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:3.1.17")
    testImplementation("com.zegreatrob.testmints:async:3.1.17")
    testImplementation("com.zegreatrob.testmints:minassert:3.1.17")
    testImplementation("com.zegreatrob.testmints:minspy:3.1.17")
}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
}
