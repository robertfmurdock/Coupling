package com.zegreatrob.coupling.plugins

plugins {
    kotlin("js")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
}

val toolsExtension = project.extensions.create("jstools", JsToolsExtension::class, loadPackageJson())

kotlin {
    js {
        useCommonJs()
        binaries.executable()
    }
}

dependencies {
    toolsExtension.packageJson.dependencies()?.forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    toolsExtension.packageJson.devDependencies()?.forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
    implementation(enforcedPlatform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:0.0.1-pre.281-kotlin-1.6.10"))
    implementation(enforcedPlatform("com.zegreatrob.testmints:testmints-bom:5.3.15"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-serialization-bom:1.3.1"))
    implementation(enforcedPlatform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.5.2"))
}
