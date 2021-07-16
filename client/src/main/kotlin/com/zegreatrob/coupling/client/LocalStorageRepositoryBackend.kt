package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User

class LocalStorageRepositoryBackend {
    val tribe by localBackend(Record<Tribe>::toSerializable, JsonTribeRecord::toModelRecord)
    val player by localBackend(TribeRecord<Player>::toSerializable, JsonPlayerRecord::toModel)
    val pairAssignments by localBackend(
        TribeRecord<PairAssignmentDocument>::toSerializable,
        JsonPairAssignmentDocumentRecord::toModel
    )
    val pin by localBackend(TribeRecord<Pin>::toSerializable, JsonPinRecord::toModel)
    val user by localBackend(Record<User>::toSerializable, JsonUserRecord::toModel)
}
