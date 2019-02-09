package com.zegreatrob.coupling.server.entity.tribe

import assertIsEqualTo
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.PairingRule
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.entity.tribe.MongoTribeRepository
import com.zegreatrob.coupling.server.MonkToolkit
import exerciseAsync
import setupAsync
import testAsync
import verifyAsync
import kotlin.test.Ignore
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoTribeRepositoryTest"

class MongoTribeRepositoryTest {

    companion object : MongoTribeRepository, MonkToolkit {
        override val jsRepository: dynamic by lazy<dynamic> { jsRepository(mongoUrl) }
        val tribeCollection by lazy<dynamic> { getCollection("tribe", mongoUrl) }
    }

    @Test
    @Ignore
    fun canSaveAndLoadTribe() = testAsync {
        setupAsync(object {
            val tribe = KtTribe(TribeId(id()), PairingRule.PreferDifferentBadge)
        }) exerciseAsync {
            save(tribe)
            getTribeAsync(tribe.id).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(tribe)
        }
    }
}