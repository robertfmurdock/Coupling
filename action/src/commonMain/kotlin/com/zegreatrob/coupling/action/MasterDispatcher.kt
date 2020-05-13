package com.zegreatrob.coupling.action

interface MasterDispatcher {
    operator fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D): R

    companion object : MasterDispatcher {
        override fun <C : SuccessfulExecutableAction<D, R>, D, R> invoke(command: C, dispatcher: D) =
            command.execute(dispatcher).value

    }
}
