package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeProvider
import com.soywiz.klock.days
import com.soywiz.klock.hours
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.data
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.model.tribe.with
import com.zegreatrob.coupling.model.tribeRecord
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.*
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubSimplePairAssignmentDocument
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.asyncTestTemplate
import kotlinx.coroutines.await
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

private typealias MongoDocumentContext = TribeContextMint<
        MongoPairAssignmentDocumentRepositoryTest.Companion.MongoPairAssignmentDocumentRepositoryTestAnchor
        >

class MongoPairAssignmentDocumentRepositoryTest :
    PairAssignmentDocumentRepositoryValidator<
            MongoPairAssignmentDocumentRepositoryTest.Companion.MongoPairAssignmentDocumentRepositoryTestAnchor> {

    override val repositorySetup =
        asyncTestTemplate<TribeContext<MongoPairAssignmentDocumentRepositoryTestAnchor>> { test ->
            val user = stubUser()
            val clock = MagicClock()
            val repositoryWithDb = MongoPairAssignmentDocumentRepositoryTestAnchor(user.email, clock)
            try {
                test(TribeContextData(repositoryWithDb, stubTribeId(), clock, user))
            } finally {
                repositoryWithDb.db.close().unsafeCast<Promise<Unit>>().await()
            }
        }

    companion object {

        class MongoPairAssignmentDocumentRepositoryTestAnchor(
            override val userId: String,
            override val clock: TimeProvider
        ) : MongoPairAssignmentDocumentRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            val historyCollection: dynamic by lazy { getCollection("history", db) }
            suspend fun getDbHistory(tribeId: TribeId) =
                historyCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

    }

    @Test
    fun whenFindingLegacyFormatWillCorrectlyLoad() = repositorySetup(object : MongoDocumentContext() {
        val documentId = "b1988bc3-2d58-4dcf-a51f-913d1cce3b50"
        val todayDate = Date()
        val data by lazy {
            json(
                "id" to documentId,
                "date" to todayDate,
                "pairs" to arrayOf(
                    arrayOf(
                        json(
                            "id" to "d52b7390-6b65-4733-97e1-07190bf730a0",
                            "name" to "Tim 9",
                            "email" to "tim@tim.meat",
                            "badge" to 1,
                            "callSignAdjective" to "Spicy",
                            "callSignNoun" to "Meatball",
                            "imageURL" to "italian.jpg",
                            "pins" to emptyArray<Json>()
                        )
                    )
                ),
                "tribe" to tribeId.value,
                "timestamp" to todayDate,
                "modifiedByUsername" to "user-147"
            )
        }
    }.bind()) {
        repository.historyCollection.insert(data).unsafeCast<Promise<Unit>>().await()
    } exercise {
        repository.getPairAssignments(tribeId)
    } verify { result ->
        result.data().map { it.document }
            .assertIsEqualTo(
                listOf(
                    PairAssignmentDocument(
                        PairAssignmentDocumentId(documentId),
                        todayDate.toDateTime(),
                        listOf(
                            pairOf(
                                Player(
                                    "d52b7390-6b65-4733-97e1-07190bf730a0",
                                    1,
                                    "Tim 9",
                                    "tim@tim.meat",
                                    "Spicy",
                                    "Meatball",
                                    "italian.jpg"
                                )
                            ).withPins()
                        )
                    )
                )
            )
    }

    @Test
    fun savingTheSameDocTwiceWillNotDeleteOriginalRecord() = repositorySetup(object : MongoDocumentContext() {
        val originalDateTime = DateTime.now()
        val pairAssignmentDocument = stubSimplePairAssignmentDocument(date = originalDateTime).second
        val updatedDateTime = originalDateTime.plus(3.days)
        val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
    }.bind()) {
        repository.save(tribeId.with(pairAssignmentDocument))
    } exercise {
        repository.save(tribeId.with(updatedDocument))
        repository.getDbHistory(tribeId)
    } verify { result ->
        result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date?>()?.toDateTime() }
            .map { it["date"].unsafeCast<Date?>()?.toDateTime() }
            .assertIsEqualTo(
                listOf(
                    updatedDateTime,
                    originalDateTime
                )
            )
    }

    @Test
    fun getRecordsWillIncludeRevisionsAndDeletions() = repositorySetup(object : MongoDocumentContext() {
        val initialTimestamp = DateTime.now().minus(3.days)
        val pairAssignmentDocument = stubPairAssignmentDoc()
        val updatedTimestamp = initialTimestamp.plus(3.hours)
        val updatedDocument = pairAssignmentDocument.copy(pairs = emptyList())
    }.bind()) {
        clock.currentTime = initialTimestamp
        repository.save(tribeId.with(pairAssignmentDocument))
        clock.currentTime = updatedTimestamp
        repository.delete(tribeId, pairAssignmentDocument.id!!)
        repository.save(tribeId.with(updatedDocument))
    } exercise {
        repository.getPairAssignmentRecords(tribeId)
    } verify { result ->
        result.assertContains(
            tribeRecord(tribeId, pairAssignmentDocument, user.email, false, initialTimestamp)
        ).assertContains(
            tribeRecord(tribeId, pairAssignmentDocument, user.email, true, updatedTimestamp)
        ).assertContains(tribeRecord(tribeId, updatedDocument, user.email, false, updatedTimestamp))
            .size
            .assertIsEqualTo(3)
    }
}
