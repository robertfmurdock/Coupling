package com.zegreatrob.coupling.actionFunc.async

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.asyncSetup
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test

private typealias MultiplyActionDispatcher = suspend (SuspendActionTest.MultiplyAction) -> Int

class SuspendActionTest : SuspendActionExecuteSyntax {

    data class MultiplyAction(val left: Int, val right: Int) : SimpleSuspendAction<MultiplyActionDispatcher, Int> {
        override val performFunc = link(MultiplyActionDispatcher::invoke)
    }

    suspend fun sillyMultiplyImplementation(action: MultiplyAction) = coroutineScope {
        (1..action.left)
            .fold(0) { current, _ -> current + action.right }
    }

    @Test
    fun usingTheActionWithTheDispatcherDoesTheWorkOfTheDispatchFunction() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val dispatcher: MultiplyActionDispatcher = ::sillyMultiplyImplementation
    }) exercise {
        dispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(6)
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = asyncSetup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = 42
        val spy = SpyData<MultiplyAction, Int>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher: MultiplyActionDispatcher = { spy.spyFunction(it) }
    }) exercise {
        spyDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }
}
