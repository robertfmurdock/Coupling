package com.zegreatrob.coupling.build

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult

class CustomTestListener : org.gradle.api.tasks.testing.TestListener {

    override fun beforeTest(testDescriptor: TestDescriptor?) {
    }

    override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {
    }

    override fun beforeSuite(suite: TestDescriptor?) {
    }

    override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {
    }

}