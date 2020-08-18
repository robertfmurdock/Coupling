import com.zegreatrob.coupling.build.BuildConstants.testmintsVersion
import com.zegreatrob.coupling.build.loadPackageJson

plugins {
    kotlin("js")
    kotlin("plugin.serialization") version "1.4.0"
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
    implementation("com.zegreatrob.testmints:minreact:+")
    implementation("com.zegreatrob.testmints:react-data-loader:+")
    implementation("com.zegreatrob.testmints:action:+")
    implementation("com.zegreatrob.testmints:action-async:+")
    implementation("com.soywiz.korlibs.klock:klock:1.12.0")
    implementation("com.benasher44:uuid:0.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.1")
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-css:1.0.0-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.111-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.111-kotlin-1.4.0")

    testImplementation(project(":stub-model"))
    testImplementation(project(":test-logging"))
    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    testImplementation("com.zegreatrob.testmints:minenzyme:$testmintsVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-common")
    testImplementation("org.jetbrains.kotlin:kotlin-test-js")
    testImplementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
    testImplementation("com.zegreatrob.testmints:standard:$testmintsVersion")
    testImplementation("com.zegreatrob.testmints:async:$testmintsVersion")
    testImplementation("com.zegreatrob.testmints:minassert:$testmintsVersion")
    testImplementation("com.zegreatrob.testmints:minspy:$testmintsVersion")
}

val nodeEnv = System.getenv("COUPLING_NODE_ENV") ?: "production"

tasks {
}
