package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.json.*
import com.zegreatrob.coupling.model.Record
import com.zegreatrob.coupling.model.TribeRecord
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.user.User
import kotlin.js.Json

class LocalStorageRepositoryBackend {
    val tribe by localBackend(Record<Tribe>::toJson, Json::toTribeRecord)
    val player by localBackend(TribeRecord<Player>::toJson, Json::toPlayerRecord)
    val pairAssignments by localBackend(
        TribeRecord<PairAssignmentDocument>::toJson,
        Json::toPairAssignmentDocumentRecord
    )
    val pin by localBackend(TribeRecord<Pin>::toJson, Json::toPinRecord)
    val user by localBackend(Record<User>::toJson, Json::toUserRecord)
}
