package com.zegreatrob.coupling.plugins

import com.zegreatrob.coupling.plugins.conventions.KotlinConventions

plugins {
    kotlin("jvm")
    id("com.zegreatrob.coupling.plugins.versioning")
    id("com.zegreatrob.coupling.plugins.reports")
    id("com.zegreatrob.coupling.plugins.testLogging")
    id("com.zegreatrob.coupling.plugins.linter")
    id("com.zegreatrob.testmints.logs.mint-logs")
}

version = "0.0.0"

kotlin {
    KotlinConventions.applyStrictCompilation(this)
    KotlinConventions.applyCommonOptIns(this)
}

KotlinConventions.applyCommonDependencies(project)
