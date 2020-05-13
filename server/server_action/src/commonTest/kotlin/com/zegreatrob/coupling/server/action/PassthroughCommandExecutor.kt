package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.action.*
import com.zegreatrob.coupling.action.CommandExecutor
import com.zegreatrob.minspy.Spy
import com.zegreatrob.minspy.SpyData
import kotlin.reflect.KClass

fun <D, C : ExecutableAction<D, R>, R> stubCommandExecutor(@Suppress("UNUSED_PARAMETER") kClass: KClass<C>) =
    StubCommandExecutor<D, C, R>()

class StubCommandExecutor<D, C : ExecutableAction<D, R>, R> : CommandExecutor<D>, Spy<C, Result<R>> by SpyData() {

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : ExecutableAction<D, R>, R> execute(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? Result<R> }
        ?: NotFoundResult("Stub not prepared for $command")

    @Suppress("UNCHECKED_CAST")
    override fun <C2 : SuccessfulExecutableAction<D, R>, R> execute(command: C2) = (command as? C)
        ?.let { spyFunction(command) as? SuccessfulResult<R> }
        ?.value
        ?: throw Exception("Stub not prepared for $command")
}
