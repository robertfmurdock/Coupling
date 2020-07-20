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
    implementation(npm("enzyme", "^3.11.0"))
    implementation(npm("enzyme-adapter-react-16", "^1.15.2"))
    implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.3.72")
    testImplementation("com.zegreatrob.testmints:standard:+")
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
