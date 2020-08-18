import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        js { nodejs() }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":action"))
                api(project(":repository"))
                api("com.zegreatrob.testmints:action:2.2.14")
                api("com.zegreatrob.testmints:action-async:2.2.14")
                implementation("com.benasher44:uuid:0.1.0")
                implementation("com.soywiz.korlibs.klock:klock:1.10.6")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")
                implementation("io.github.microutils:kotlin-logging-common:1.8.3")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":stub-model"))
                api(project(":test-action"))
                implementation("com.zegreatrob.testmints:standard:2.2.14")
                implementation("com.zegreatrob.testmints:async:2.2.14")
                implementation("com.zegreatrob.testmints:minassert:2.2.14")
                implementation("com.zegreatrob.testmints:minspy:2.2.14")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.3.8")
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

}
