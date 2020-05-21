package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.actionFunc.*
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import kotlin.reflect.KClass

fun <D, C : DispatchableAction<D, Result<R>>, R> stubCommandExecutor(@Suppress("UNUSED_PARAMETER") kClass: KClass<C>) =
    StubCommandExecutor<D, C, R>()

class StubCommandExecutor<D, C : DispatchableAction<D, Result<R>>, R> : ResultCommandExecutor<D>, Spy<C, Result<R>> by SpyData() {

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : ExecutableResultAction<D, R>, R> invoke(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? Result<R> }
        ?: NotFoundResult("Stub not prepared for $command")

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : SuccessfulExecutableAction<D, R>, R> invoke(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? SuccessfulResult<R> }
        ?.value
        ?: throw Exception("Stub not prepared for $command")

    @Suppress("UNCHECKED_CAST")
    override suspend fun <C2 : SuspendResultAction<D, R>, R> invoke(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? Result<R> }
        ?: NotFoundResult("Stub not prepared for $command")

    override fun <C : ExecutableAction<D, R>, R> invoke(command: C): R {
        TODO("Not yet implemented")
    }

    override suspend fun <C : SuspendAction<D, R>, R> invoke(command: C): R {
        TODO("Not yet implemented")
    }
}
