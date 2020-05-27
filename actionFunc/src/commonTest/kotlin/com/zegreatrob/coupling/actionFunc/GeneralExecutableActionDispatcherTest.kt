package com.zegreatrob.coupling.actionFunc

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class GeneralExecutableActionDispatcherTest {

    data class MultiplyAction(val left: Int, val right: Int) : SimpleExecutableAction<MultiplyActionDispatcher, Int> {
        override val performFunc = link(MultiplyActionDispatcher::perform)
    }

    interface MultiplyActionDispatcher {
        fun perform(action: MultiplyAction): Int
    }

    @Test
    fun generalDispatcherSyntaxAllowsInterceptionOfActionExecutionIncludingReplacingResult() =
        setup(object : GeneralExecutableActionDispatcherSyntax {
            val expectedReplacedResult = 127
            override val generalDispatcher = generalDispatcherSpy()
                .apply { spyWillReturn(expectedReplacedResult) }
            val action = MultiplyAction(6, 7)
            val multiplyDispatcher = multiplyDispatcherSpy()
        }) exercise {
            multiplyDispatcher.execute(action)
        } verify { result ->
            result.assertIsEqualTo(expectedReplacedResult)
            generalDispatcher.spyReceivedValues
                .assertIsEqualTo(listOf(action to multiplyDispatcher))
            multiplyDispatcher.spyReceivedValues
                .assertIsEqualTo(emptyList<Any>())
        }

    private fun multiplyDispatcherSpy() = object : MultiplyActionDispatcher, Spy<MultiplyAction, Int> by SpyData() {
        override fun perform(action: MultiplyAction) = spyFunction(action)
    }

    private fun generalDispatcherSpy() = object : GeneralExecutableActionDispatcher,
        Spy<Pair<ExecutableAction<*, *>, *>, Any> by SpyData() {
        @Suppress("UNCHECKED_CAST")
        override fun <D, R> dispatch(action: ExecutableAction<D, R>, dispatcher: D): R =
            spyFunction(action to dispatcher) as R
    }

}