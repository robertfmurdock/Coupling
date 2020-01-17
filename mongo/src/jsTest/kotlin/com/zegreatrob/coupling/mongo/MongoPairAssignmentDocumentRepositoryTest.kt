package com.zegreatrob.coupling.mongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.js.toDateTime
import com.zegreatrob.coupling.model.pairassignmentdocument.*
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.mongo.pairassignments.MongoPairAssignmentDocumentRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import stubPairAssignmentDoc
import stubSimplePairAssignmentDocument
import kotlin.js.Date
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

class MongoPairAssignmentDocumentRepositoryTest {

    companion object {

        private fun repositoryWithDb() = MongoPairAssignmentDocumentRepositoryTestAnchor()

        class MongoPairAssignmentDocumentRepositoryTestAnchor : MongoPairAssignmentDocumentRepository, MonkToolkit {
            val db = getDb(mongoUrl)
            override val jsRepository: dynamic = jsRepository(db)
            override val userEmail: String = "user-${Random.nextInt(200)}"
            val historyCollection: dynamic by lazy<dynamic> { getCollection("history", db) }

            suspend fun dropHistory() {
                historyCollection.drop().unsafeCast<Promise<Unit>>().await()
            }

            suspend fun getDbHistory(tribeId: TribeId) =
                historyCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()
        }

        private suspend inline fun withRepository(block: MongoPairAssignmentDocumentRepositoryTestAnchor.() -> Unit) {
            val repositoryWithDb = repositoryWithDb()
            try {
                with(repositoryWithDb, block)
            } finally {
                repositoryWithDb.db.close().unsafeCast<Promise<Unit>>().await()
            }
        }
    }

    @Test
    fun givenSimplePairSaveAndGetWorks() = testAsync {
        withRepository {
            setupAsync(object {
                val tribeId = TribeId("tribe-id-99")
                val document = stubSimplePairAssignmentDocument().second
            }) {
                dropHistory()
            } exerciseAsync {
                save(document.with(tribeId))
                getPairAssignments(tribeId)
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(document))
            }
        }
    }

    @Test
    fun differentDocumentsBothShowUpInGetFromNewestToOldestByDate() = testAsync {
        withRepository {
            setupAsync(object {
                val tribeId = TribeId("tribe-id-99")
                val oldest = stubSimplePairAssignmentDocument(date = DateTime.now().minus(3.days)).second
                val middle = stubSimplePairAssignmentDocument(date = DateTime.now()).second
                val newest = stubSimplePairAssignmentDocument(date = DateTime.now().plus(2.days)).second
            }) {
                dropHistory()
                listOf(middle, oldest, newest).forEach { save(it.with(tribeId)) }
            } exerciseAsync {
                getPairAssignments(tribeId)
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(newest, middle, oldest))
            }
        }
    }

    @Test
    fun willAssignIdWhenDocumentHasNone() = testAsync {
        withRepository {
            setupAsync(object {
                val tribeId = TribeId("tribe-id-99")
                val tribeIdDocument = stubPairAssignmentDoc().copy(id = null).with(tribeId)
            }) {
                dropHistory()
            } exerciseAsync {
                save(tribeIdDocument)
                getPairAssignments(tribeId)
            } verifyAsync { result ->
                val resultId = result.getOrNull(0)?.id
                assertNotNull(resultId)
                result.assertIsEqualTo(listOf(tribeIdDocument.document.copy(id = resultId)))
            }
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = testAsync {
        withRepository {
            setupAsync(object {
                val tribeId = TribeId("tribe-id-99")
                private val pair = stubSimplePairAssignmentDocument()
                val id = pair.first
                val document = pair.second.with(tribeId)
            }) {
                dropHistory()
                save(document)
            } exerciseAsync {
                delete(tribeId, id)
            } verifyAsync { result ->
                result.assertIsEqualTo(true)
                getPairAssignments(tribeId)
                    .assertIsEqualTo(emptyList())
            }
        }
    }

    @Test
    fun deleteWhenRecordDoesNotExistWillReturnFalse() = testAsync {
        withRepository {
            setupAsync(object {
                private val pair = stubSimplePairAssignmentDocument()
                val id = pair.first
            }) exerciseAsync {
                delete(TribeId("sldkjf"), id)
            } verifyAsync { result ->
                result.assertIsEqualTo(false)
            }
        }
    }

    @Test
    fun whenFindingLegacyFormatWillCorrectlyLoad() = testAsync {
        withRepository {
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
                result.assertIsEqualTo(
                    listOf(
                        PairAssignmentDocument(
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
                                )
                                    .withPins()
                            ),
                            PairAssignmentDocumentId(documentId)
                        )
                    )
                )
            }
        }
    }

    class SavingTheSameDocumentTwice {

        private suspend fun MongoPairAssignmentDocumentRepositoryTestAnchor.setupSavedDocument() = setupAsync(object {
            val tribeId = TribeId("boo")
            val originalDateTime = DateTime.now()
            val pairAssignmentDocument = stubSimplePairAssignmentDocument(date = originalDateTime).second
            val updatedDateTime = originalDateTime.plus(3.days)
            val updatedDocument = pairAssignmentDocument.copy(date = updatedDateTime)
        }) {
            dropHistory()
            save(pairAssignmentDocument.with(tribeId))
        }

        @Test
        fun willNotDeleteOriginalRecord() = testAsync {
            withRepository {
                setupSavedDocument() exerciseAsync {
                    save(updatedDocument.with(tribeId))
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
        fun getWillOnlyReturnTheUpdatedDocument() = testAsync {
            withRepository {
                setupSavedDocument() exerciseAsync {
                    save(updatedDocument.with(tribeId))
                    getPairAssignments(tribeId)
                } verifyAsync { result ->
                    result.assertIsEqualTo(listOf(updatedDocument))
                }
            }
        }
    }
}