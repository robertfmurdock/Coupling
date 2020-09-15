package com.zegreatrob.coupling.server.express.route

import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.user.User

interface LiveInfoRepository {
    fun get(tribeId: TribeId): LiveInfo
    fun save(tribeId: TribeId, info: LiveInfo)
}

data class LiveInfo(val users: List<User>, val currentPairAssignmentDocument: PairAssignmentDocument?)
