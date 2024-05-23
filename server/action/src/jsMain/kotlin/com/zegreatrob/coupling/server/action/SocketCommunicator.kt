package com.zegreatrob.coupling.server.action

import com.zegreatrob.coupling.model.Message

interface SocketCommunicator {
    suspend fun sendMessageAndReturnIdWhenFail(connectionId: String, message: Message): String?
}
