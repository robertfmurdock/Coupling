package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule.Companion.toValue
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.DbRecordLoadSyntax
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlin.js.Json
import kotlin.js.json

interface MongoTribeRepository : DbRecordLoadSyntax {

    val jsRepository: dynamic
    private val tribeCollection: dynamic get() = jsRepository.tribeCollection

    fun save(tribe: KtTribe) {
        tribe.toDbJson()
    }


    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe?> {
        return CompletableDeferred(value = null)
    }
}

private fun KtTribe.toDbJson(): Json {
    return json(
            "id" to id,
            "pairingRule" to toValue(pairingRule)
    )
}
