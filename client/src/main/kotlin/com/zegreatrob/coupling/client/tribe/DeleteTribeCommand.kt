package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.common.Action
import com.zegreatrob.coupling.common.entity.tribe.TribeId

data class DeleteTribeCommand(val tribeId: TribeId) : Action

interface DeleteTribeCommandDispatcher : TribeIdDeleteSyntax {

    suspend fun DeleteTribeCommand.perform() = tribeId.deleteAsync().await()

}
