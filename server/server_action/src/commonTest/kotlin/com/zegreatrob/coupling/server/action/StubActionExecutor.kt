package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.actionFunc.ActionExecutor
import com.zegreatrob.coupling.actionFunc.DispatchableAction
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import kotlin.reflect.KClass

fun <D, C : DispatchableAction<D, R>, R> stubActionExecutor(@Suppress("UNUSED_PARAMETER") kClass: KClass<C>) =
    StubActionExecutor<D, C, R>()

class StubActionExecutor<D, C : DispatchableAction<D, R>, R> : ActionExecutor<D>, Spy<C, R> by SpyData() {

    @Suppress("UNCHECKED_CAST")
    override fun <R> invoke(action: ExecutableAction<D, R>): R = (action as? C)
        ?.let { spyFunction(action) as? R }
        ?: throw Exception("Not configured")

    @Suppress("UNCHECKED_CAST")
    override suspend fun <R> invoke(action: SuspendAction<D, R>): R = (action as? C)
        ?.let { spyFunction(action) as? R }
        ?: throw Exception("Stub not prepared for $action")

}
