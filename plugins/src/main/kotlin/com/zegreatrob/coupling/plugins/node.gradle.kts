package com.zegreatrob.coupling.plugins

import org.gradle.kotlin.dsl.create

val toolsExtension = project.extensions.create("nodetools", NodeExtension::class)
