package com.zegreatrob.coupling.action

class DeleteBoostCommand : SimpleSuspendResultAction<DeleteBoostCommand.Dispatcher, Unit> {
    override val performFunc = link(Dispatcher::perform)

    interface Dispatcher {
        suspend fun perform(command: DeleteBoostCommand): SuccessfulResult<Unit>
    }
}
