import com.zegreatrob.coupling.build.BuildConstants
import com.zegreatrob.coupling.build.UnpackGradleDependenciesTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.github.node-gradle.node")
    id("smol-js")
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/soywiz/soywiz") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

kotlin {
    targets {
        add(presets["js"].createTarget("js"))
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":commonKt"))
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")
                implementation("com.soywiz:klock:1.1.1")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":test-style"))
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
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
        dependsOn(":test-style:assemble", ":commonKt:assemble")
    }

}
