import com.zegreatrob.coupling.build.BuildConstants
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    targets {
        js {
            nodejs()
            useCommonJs()
        }
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(project(":model"))
                api(project(":action"))
                api(project(":repository"))
                api("com.zegreatrob.testmints:action:4.0.6")
                api("com.zegreatrob.testmints:action-async:4.0.6")
                implementation("com.benasher44:uuid:0.3.0")
                implementation("com.soywiz.korlibs.klock:klock:2.1.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js:${BuildConstants.kotlinVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0-RC")
                implementation("io.github.microutils:kotlin-logging:2.0.6")
            }
        }
        getByName("commonTest") {
            dependencies {
                api(project(":stub-model"))
                api(project(":test-action"))
                implementation("com.zegreatrob.testmints:standard:4.0.6")
                implementation("com.zegreatrob.testmints:async:4.0.6")
                implementation("com.zegreatrob.testmints:minassert:4.0.6")
                implementation("com.zegreatrob.testmints:minspy:4.0.6")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
            }
        }

        val jsMain by getting {
            dependencies {
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
