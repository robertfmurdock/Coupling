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
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":engine"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.2")
                implementation("com.soywiz:klock:1.1.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation("com.zegreatrob.testmints:standard:+")
                implementation("com.zegreatrob.testmints:minassert:+")
                implementation("com.zegreatrob.testmints:async-js:+")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.2")
            }
        }
        val jsTest by getting {
            dependencies {
                api(project(":logging"))
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
        dependsOn(":commonKt:assemble")
    }

}
