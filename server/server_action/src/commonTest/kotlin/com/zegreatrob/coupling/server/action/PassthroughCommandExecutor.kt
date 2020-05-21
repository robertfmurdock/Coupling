package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.actionFunc.*
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import kotlin.reflect.KClass

fun <D, C : DispatchableAction<D, R>, R> stubCommandExecutor(@Suppress("UNUSED_PARAMETER") kClass: KClass<C>) =
    StubCommandExecutor<D, C, R>()

class StubCommandExecutor<D, C : DispatchableAction<D, R>, R> : ResultCommandExecutor<D>, Spy<C, R> by SpyData() {

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : ExecutableResultAction<D, R>, R> invoke(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? Result<R> }
        ?: NotFoundResult("Stub not prepared for $command")
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <C2 : SuspendResultAction<D, R>, R> invoke(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? Result<R> }
        ?: NotFoundResult("Stub not prepared for $command")

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : ExecutableAction<D, R>, R> invoke(command: C2): R = (command as? C)
        ?.let { spyFunction(command) as? R }
        ?: throw Exception("Not configured")

    override suspend fun <C : SuspendAction<D, R>, R> invoke(command: C): R {
        TODO("Not yet implemented")
    }
}
