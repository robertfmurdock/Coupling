package com.zegreatrob.coupling.e2e

import com.zegreatrob.testmints.invoke
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