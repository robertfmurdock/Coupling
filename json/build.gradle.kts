import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.github.node-gradle.node")
    id("smol-js")
}

kotlin {
    targets {
        jvm()
        js()
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation(project(":model"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${BuildConstants.kotlinVersion}")
                implementation("com.soywiz.korlibs.klock:klock:1.8.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation(project(":test-logging"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("reflect", BuildConstants.kotlinVersion))
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit5")

                implementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-js")
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

    val unpackJsGradleDependencies by getting(UnpackGradleDependenciesTask::class) {
        dependsOn(":test-logging:assemble", ":model:assemble")
    }

}
