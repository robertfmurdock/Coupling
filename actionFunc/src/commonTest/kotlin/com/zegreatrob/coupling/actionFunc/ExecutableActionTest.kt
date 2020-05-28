package com.zegreatrob.coupling.actionFunc

import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.setup
import kotlin.test.Test


class ExecutableActionTest : ExecutableActionExecuteSyntax {

    data class MultiplyAction(val left: Int, val right: Int) : SimpleExecutableAction<MultiplyActionDispatcher, Int> {
        override val performFunc = link(MultiplyActionDispatcher::perform)
    }

    interface MultiplyActionDispatcher {
        fun perform(action: MultiplyAction): Int
    }

    @Test
    fun executingActionMerelyPassesActionToDispatcherWhereWorkCanBeDone() = setup(object {
        val action = MultiplyAction(2, 3)
        val expectedReturn = 42
        val spy = SpyData<MultiplyAction, Int>().apply { spyWillReturn(expectedReturn) }
        val spyDispatcher = object : MultiplyActionDispatcher {
            override fun perform(action: MultiplyAction) = spy.spyFunction(action)
        }
    }) exercise {
        spyDispatcher.execute(action)
    } verify { result ->
        result.assertIsEqualTo(expectedReturn)
        spy.spyReceivedValues.assertIsEqualTo(listOf(action))
    }
}

