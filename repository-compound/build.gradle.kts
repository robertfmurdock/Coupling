import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
}

kotlin {
    targets {
        jvm()
        js { nodejs() }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":model"))
                api(project(":repository-core"))
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":test-logging"))
                implementation(project(":repository-memory"))
                implementation(project(":repository-validation"))
                implementation("com.zegreatrob.testmints:standard:5.3.14")
                implementation("com.zegreatrob.testmints:minassert:5.3.14")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("reflect", BuildConstants.kotlinVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
            }
        }

        val jsMain by getting {
            dependencies {
                api("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("com.zegreatrob.testmints:async:5.3.14")
            }
        }
    }
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "umd"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
