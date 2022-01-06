package com.zegreatrob.coupling.plugins

import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

afterEvaluate {
    tasks.withType(KotlinJsTest::class) {
        addTestListener(JsonLoggingTestListener(path))
    }
}
