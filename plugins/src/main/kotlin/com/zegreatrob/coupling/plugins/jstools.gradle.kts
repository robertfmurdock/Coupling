package com.zegreatrob.coupling.plugins

plugins {
    kotlin("js")
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
}
