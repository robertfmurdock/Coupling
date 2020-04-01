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
import com.zegreatrob.coupling.model.user.User
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.pairassignmentdocument.PairAssignmentDocumentRepository
import com.zegreatrob.coupling.repository.validation.MagicClock
import com.zegreatrob.coupling.repository.validation.PairAssignmentDocumentRepositoryValidator
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import com.zegreatrob.coupling.stubmodel.stubPairAssignmentDoc
import com.zegreatrob.coupling.stubmodel.stubSimplePairAssignmentDocument
import com.zegreatrob.coupling.stubmodel.stubTribeId
import com.zegreatrob.coupling.stubmodel.stubUser
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

class MongoPairAssignmentDocumentRepositoryTest : PairAssignmentDocumentRepositoryValidator {

    override suspend fun withRepository(
        clock: TimeProvider,
        handler: suspend (PairAssignmentDocumentRepository, TribeId, User) -> Unit
    ) {
        val user = stubUser()
        withMongoRepository(user, clock) { handler(this, stubTribeId(), user) }
    }

    companion object {

        private fun repositoryWithDb(
            user: User = stubUser(),
            clock: TimeProvider = TimeProvider
        ) = MongoPairAssignmentDocumentRepositoryTestAnchor(user.email, clock)

        class MongoPairAssignmentDocumentRepositoryTestAnchor(
            override val userEmail: String,
            override val clock: TimeProvider
        ) : MongoPairAssignmentDocumentRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            val historyCollection: dynamic by lazy<dynamic> { getCollection("history", db) }

            suspend fun dropHistory() {
                historyCollection.drop().unsafeCast<Promise<Unit>>().await()
            }

            suspend fun getDbHistory(tribeId: TribeId) =
                historyCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

        private suspend inline fun withMongoRepository(
            user: User = stubUser(),
            clock: TimeProvider = TimeProvider,
            block: MongoPairAssignmentDocumentRepositoryTestAnchor.() -> Unit
        ) {
            val repositoryWithDb = repositoryWithDb(user, clock)
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close().unsafeCast<Promise<Unit>>().await()
            }
        }
    }

    @Test
    fun whenFindingLegacyFormatWillCorrectlyLoad() = testAsync {
        withMongoRepository {
            dropHistory()
            setupAsync(object {
                val documentId = "b1988bc3-2d58-4dcf-a51f-913d1cce3b50"
                val tribeId = "boo"
                val todayDate = Date()
                val data = json(
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
                    "tribe" to tribeId,
                    "timestamp" to todayDate,
                    "modifiedByUsername" to "user-147"
                )

            }) {
                historyCollection.insert(data).unsafeCast<Promise<Unit>>().await()
            } exerciseAsync {
                getPairAssignments(TribeId(tribeId))
            } verifyAsync { result ->
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
        }
    }

    @Test
    fun savingTheSameDocTwiceWillNotDeleteOriginalRecord() = testAsync {
        withMongoRepository {
            setupAsync(object {
                val tribeId = TribeId("boo")
                val originalDateTime = DateTime.now()
                val pairAssignmentDocument = stubSimplePairAssignmentDocument(date = originalDateTime).second
                val updatedDateTime = originalDateTime.plus(3.days)
                val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
            }) {
                dropHistory()
                save(tribeId.with(pairAssignmentDocument))
            } exerciseAsync {
                save(tribeId.with(updatedDocument))
                getDbHistory(tribeId)
            } verifyAsync { result ->
                result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date?>()?.toDateTime() }
                    .map { it["date"].unsafeCast<Date?>()?.toDateTime() }
                    .assertIsEqualTo(
                        listOf(
                            updatedDateTime,
                            originalDateTime
                        )
                    )
            }
        }
    }

    @Test
    fun getRecordsWillReturnAllRecordsIncludingRevisionsAndDeletions() = testAsync {
        val clock = MagicClock()
        val user = stubUser()
        withMongoRepository(user = user, clock = clock) {
            setupAsync(object {
                val tribeId = stubTribeId()
                val initialTimestamp = DateTime.now().minus(3.days)
                val pairAssignmentDocument = stubPairAssignmentDoc()
                val updatedTimestamp = initialTimestamp.plus(3.hours)
                val updatedDocument = pairAssignmentDocument.copy(pairs = emptyList())
            }) {
                clock.currentTime = initialTimestamp
                save(tribeId.with(pairAssignmentDocument))
                clock.currentTime = updatedTimestamp
                delete(tribeId, pairAssignmentDocument.id!!)
                save(tribeId.with(updatedDocument))
            } exerciseAsync {
                getPairAssignmentRecords(tribeId)
            } verifyAsync { result ->
                result.assertContains(tribeRecord(tribeId, pairAssignmentDocument, user.email, false, initialTimestamp))
                    .assertContains(tribeRecord(tribeId, pairAssignmentDocument, user.email, true, updatedTimestamp))
                    .assertContains(tribeRecord(tribeId, updatedDocument, user.email, false, updatedTimestamp))
                    .size.assertIsEqualTo(3)
            }
        }
    }

}