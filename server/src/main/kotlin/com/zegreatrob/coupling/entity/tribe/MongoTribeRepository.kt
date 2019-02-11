package com.zegreatrob.coupling.entity.tribe

import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.server.DbRecordLoadSyntax
import com.zegreatrob.coupling.server.DbRecordSaveSyntax
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.js.Json
import kotlin.js.json

interface MongoTribeRepository : DbRecordSaveSyntax, DbRecordLoadSyntax {

    val jsRepository: dynamic

    suspend fun save(tribe: KtTribe) = tribe.toDbJson()
            .let {
                it.save(jsRepository.tribesCollection)
            }

    fun getTribeAsync(tribeId: TribeId): Deferred<KtTribe?> = GlobalScope.async {
        findByQuery(json("id" to tribeId.value), jsRepository.tribesCollection)
                .firstOrNull()
                ?.toTribe()
    }

    private fun KtTribe.toDbJson() = json(
            "id" to id.value,
            "pairingRule" to toValue(pairingRule),
            "name" to name,
            "defaultBadgeName" to defaultBadgeName,
            "alternateBadgeName" to alternateBadgeName
    )

    private fun Json.toTribe(): KtTribe = KtTribe(
            id = TribeId(this["id"].toString()),
            pairingRule = PairingRule.fromValue(this["pairingRule"] as? Int),
            name = this["name"]?.toString(),
            defaultBadgeName = this["defaultBadgeName"]?.toString(),
            alternateBadgeName = this["alternateBadgeName"]?.toString()
    )

    private fun toValue(rule: PairingRule): Int = when (rule) {
        PairingRule.LongestTime -> 1
        PairingRule.PreferDifferentBadge -> 2
    }
}

