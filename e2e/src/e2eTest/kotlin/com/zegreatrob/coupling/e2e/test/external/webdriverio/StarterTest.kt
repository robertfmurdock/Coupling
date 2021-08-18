package com.zegreatrob.coupling.e2e.test.external.webdriverio

import com.zegreatrob.testmints.setup
import kotlin.test.Test

class StarterTest {

    @Test
    fun doThing() = setup(object {
    }) exercise {
        console.log("exercise")
    } verify {
        console.log("verify")
    }
}