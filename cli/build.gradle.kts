plugins {
    application
    id("com.zegreatrob.coupling.plugins.jvm")
}

dependencies {
    implementation(project(":sdk"))
    implementation(libs.com.github.ajalt.clikt.clikt)
}
