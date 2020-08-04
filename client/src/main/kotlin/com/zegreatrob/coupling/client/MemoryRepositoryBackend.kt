package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocument
import com.zegreatrob.coupling.model.pin.TribeIdPin
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.repository.memory.SimpleRecordBackend

class MemoryRepositoryBackend {
    val tribe = SimpleRecordBackend<Tribe>()
    val player = SimpleRecordBackend<TribeIdPlayer>()
    val pairAssignments = SimpleRecordBackend<TribeIdPairAssignmentDocument>()
    val pin = SimpleRecordBackend<TribeIdPin>()
    val user = SimpleRecordBackend<User>()
}
