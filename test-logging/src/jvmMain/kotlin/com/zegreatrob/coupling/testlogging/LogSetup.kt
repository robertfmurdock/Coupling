package com.zegreatrob.coupling.testlogging

import com.zegreatrob.testmints.MintReporter
import com.zegreatrob.testmints.StandardMints
import mu.KotlinLogging
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class SetupTestLogging : BeforeAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        StandardMints.reporter = object : MintReporter {
            private val logger by lazy { KotlinLogging.logger("testmints") }
            override fun exerciseStart() = logger.info { "exerciseStart" }
            override fun exerciseFinish() = logger.info { "exerciseFinish" }
            override fun verifyStart() = logger.info { "verifyStart" }
            override fun verifyFinish() = logger.info { "verifyFinish" }
        }
    }

}