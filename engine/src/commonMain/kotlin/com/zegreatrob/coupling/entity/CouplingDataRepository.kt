package com.zegreatrob.coupling.entity

import com.zegreatrob.coupling.common.entity.pin.Pin
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.pairassignmentdocument.PairAssignmentDocumentGetter
import kotlinx.coroutines.Deferred

interface CouplingDataRepository : PairAssignmentDocumentGetter {
    fun getPinsAsync(tribeId: TribeId): Deferred<List<Pin>>
    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe>
}