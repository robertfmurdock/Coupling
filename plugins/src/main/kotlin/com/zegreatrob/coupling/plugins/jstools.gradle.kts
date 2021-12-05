package com.zegreatrob.coupling.plugins

plugins {
    kotlin("js")
}

val packageJson = loadPackageJson()

dependencies {
    packageJson.dependencies().forEach {
        implementation(npm(it.first, it.second.asText()))
    }

    packageJson.devDependencies().forEach {
        testImplementation(npm(it.first, it.second.asText()))
    }
}
