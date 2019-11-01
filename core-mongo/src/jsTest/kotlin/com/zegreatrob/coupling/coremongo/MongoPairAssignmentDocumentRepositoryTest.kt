package com.zegreatrob.coupling.coremongo

import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.internal.toDateTime
import com.zegreatrob.coupling.core.entity.pairassignmentdocument.*
import com.zegreatrob.coupling.core.entity.player.Player
import com.zegreatrob.coupling.core.entity.tribe.TribeId
import com.zegreatrob.coupling.server.MongoPairAssignmentDocumentRepository
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.await
import kotlin.js.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull

private const val mongoUrl = "localhost/MongoPairAssignmentDocumentRepositoryTest"

class MongoPairAssignmentDocumentRepositoryTest {

    companion object : MongoPairAssignmentDocumentRepository, MonkToolkit {
        override val userEmail: String = "user-${Random.nextInt(200)}"

        override val jsRepository: dynamic = jsRepository(mongoUrl)

        private val historyCollection: dynamic by lazy<dynamic> { getCollection("history", mongoUrl) }

        suspend fun dropHistory() {
            historyCollection.drop().unsafeCast<Promise<Unit>>().await()
        }

        suspend fun getDbHistory(tribeId: TribeId) =
                historyCollection.find(json("tribe" to tribeId.value)).unsafeCast<Promise<Array<Json>>>().await()

        private fun stubSimplePairAssignmentDocument(date: DateTime = DateTime.now()) =
                PairAssignmentDocumentId(id())
                        .let { id ->
                            id to stubPairAssignmentDoc(date, id)
                        }

        private fun stubPairAssignmentDoc(
                date: DateTime = DateTime.now(),
                id: PairAssignmentDocumentId? = PairAssignmentDocumentId(id())
        ) =
                PairAssignmentDocument(
                        date = date,
                        pairs = listOf(
                                PinnedCouplingPair(listOf(Player(
                                        id = "zeId",
                                        badge = 1,
                                        email = "whoop whoop",
                                        name = "Johnny",
                                        imageURL = "publicDomain.png",
                                        callSignNoun = "Wily",
                                        callSignAdjective = "Rural Wolf"
                                ).withPins()))
                        ),
                        id = id
                )
    }

    @Test
    fun givenSimplePairSaveAndGetWorks() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val document = stubSimplePairAssignmentDocument().second
        }) {
            dropHistory()
        } exerciseAsync {
            save(document.with(tribeId))
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(document))
        }
    }

    @Test
    fun differentDocumentsBothShowUpInGetFromNewestToOldestByDate() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val oldest = stubSimplePairAssignmentDocument(date = DateTime.now().minus(3.days)).second
            val middle = stubSimplePairAssignmentDocument(date = DateTime.now()).second
            val newest = stubSimplePairAssignmentDocument(date = DateTime.now().plus(2.days)).second
        }) {
            dropHistory()
            listOf(middle, oldest, newest).forEach { save(it.with(tribeId)) }
        } exerciseAsync {
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            result.assertIsEqualTo(listOf(newest, middle, oldest))
        }
    }

    @Test
    fun willAssignIdWhenDocumentHasNone() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            val tribeIdDocument = stubPairAssignmentDoc(id = null).with(tribeId)
        }) {
            dropHistory()
        } exerciseAsync {
            save(tribeIdDocument)
            getPairAssignmentsAsync(tribeId).await()
        } verifyAsync { result ->
            val resultId = result.getOrNull(0)?.id
            assertNotNull(resultId)
            result.assertIsEqualTo(listOf(tribeIdDocument.document.copy(id = resultId)))
        }
    }

    @Test
    fun saveAndDeleteThenGetWillReturnNothing() = testAsync {
        setupAsync(object {
            val tribeId = TribeId("tribe-id-99")
            private val pair = stubSimplePairAssignmentDocument()
            val id = pair.first
            val document = pair.second.with(tribeId)
        }) {
            dropHistory()
            save(document)
        } exerciseAsync {
            delete(id)
        } verifyAsync { result ->
            result.assertIsEqualTo(true)
            getPairAssignmentsAsync(tribeId).await()
                    .assertIsEqualTo(emptyList())
        }
    }

    @Test
    fun deleteWhenRecordDoesNotExistWillReturnFalse() = testAsync {
        setupAsync(object {
            private val pair = stubSimplePairAssignmentDocument()
            val id = pair.first
        }) exerciseAsync {
            delete(id)
        } verifyAsync { result ->
            result.assertIsEqualTo(false)
        }
    }

    class SavingTheSameDocumentTwice {

        private suspend fun setupSavedDocument() = setupAsync(object {
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
            setupSavedDocument() exerciseAsync {
                save(updatedDocument.with(tribeId))
                getDbHistory(tribeId)
            } verifyAsync { result ->
                result.toList().sortedByDescending { it["timestamp"].unsafeCast<Date?>()?.toDateTime() }
                        .map { it["date"].unsafeCast<Date?>()?.toDateTime() }
                        .assertIsEqualTo(listOf(
                                updatedDateTime,
                                originalDateTime
                        ))
            }
        }

        @Test
        fun getWillOnlyReturnTheUpdatedDocument() = testAsync {
            setupSavedDocument() exerciseAsync {
                save(updatedDocument.with(tribeId))
                getPairAssignmentsAsync(tribeId).await()
            } verifyAsync { result ->
                result.assertIsEqualTo(listOf(updatedDocument))
            }
        }
    }
}