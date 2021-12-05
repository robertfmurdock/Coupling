package com.zegreatrob.coupling.plugins

plugins {
    kotlin("js")
}

val toolsExtension = project.extensions.create("nodetools", NodeExtension::class)
