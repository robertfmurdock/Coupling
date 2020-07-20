import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

kotlin {
    target {
        nodejs()
    }
}

dependencies {
    api(npm("core-js", "^3.6.5"))
    api("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.3.72")
    api("org.jetbrains:kotlin-react-router-dom:5.1.2-pre.107-kotlin-1.3.72")
}

tasks {
    val compileKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
    val compileTestKotlinJs by getting(Kotlin2JsCompile::class) {
        kotlinOptions.moduleKind = "commonjs"
        kotlinOptions.sourceMap = true
        kotlinOptions.sourceMapEmbedSources = "always"
    }
}
