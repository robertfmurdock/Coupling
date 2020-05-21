package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.actionFunc.CommandExecutor
import com.zegreatrob.coupling.actionFunc.DispatchableAction
import com.zegreatrob.coupling.actionFunc.ExecutableAction
import com.zegreatrob.coupling.actionFunc.SuspendAction
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import kotlin.reflect.KClass

fun <D, C : DispatchableAction<D, R>, R> stubCommandExecutor(@Suppress("UNUSED_PARAMETER") kClass: KClass<C>) =
    StubCommandExecutor<D, C, R>()

class StubCommandExecutor<D, C : DispatchableAction<D, R>, R> : CommandExecutor<D>, Spy<C, R> by SpyData() {

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : ExecutableAction<D, R>, R> invoke(command: C2): R = (command as? C)
        ?.let { spyFunction(command) as? R }
        ?: throw Exception("Not configured")

    @Suppress("UNCHECKED_CAST")
    override suspend fun <C2 : SuspendAction<D, R>, R> invoke(command: C2): R = (command as? C)
        ?.let { spyFunction(command) as? R }
        ?: throw Exception("Stub not prepared for $command")

}
