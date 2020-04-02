package com.zegreatrob.coupling.client.pairassignments

import Spy
import SpyData
import com.benasher44.uuid.Uuid
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.pairassignments.list.HistoryProps
import com.zegreatrob.coupling.client.pairassignments.list.HistoryRenderer
import com.zegreatrob.coupling.client.pairassignments.list.HistoryStyles
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Window
import shallow
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class HistoryTest {

    private val styles = loadStyles<HistoryStyles>("pairassignments/History")

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = testAsync {
        withContext(Dispatchers.Default) {
            setupAsync(object : HistoryRenderer, PropsClassProvider<HistoryProps> by provider() {
                override val traceId: Uuid? = null
                override val pairAssignmentDocumentRepository get() = throw NotImplementedError("")
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val tribe =
                    Tribe(TribeId("me"))
                val removeSpy = object : Spy<Unit, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPairAssignmentDocumentId.delete() =
                    removeSpy.spyFunction(Unit).let { true }

                val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

                val history = listOf(
                    PairAssignmentDocument(
                        PairAssignmentDocumentId("RealId"),
                        DateTime.now(),
                        emptyList()
                    )
                )
                val wrapper = shallow(
                    HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {})
                )
            }) {
                removeSpy.spyWillReturn(Promise.resolve(Unit))
                reloadSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find<Any>(".${styles.deleteButton}").simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues.isNotEmpty()
                .assertIsEqualTo(true)
            reloadSpy.spyReceivedValues.isNotEmpty()
                .assertIsEqualTo(true)
        }
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = testAsync {
        withContext(Dispatchers.Default) {
            setupAsync(object : HistoryRenderer, PropsClassProvider<HistoryProps> by provider() {
                override val traceId: Uuid? = null
                override val pairAssignmentDocumentRepository get() = throw NotImplementedError("")
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()

                val tribe =
                    Tribe(TribeId("me"))
                val removeSpy = object : Spy<Unit, Promise<Unit>> by SpyData() {}
                override suspend fun TribeIdPairAssignmentDocumentId.delete() =
                    removeSpy.spyFunction(Unit).let { true }

                val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

                val history = listOf(
                    PairAssignmentDocument(
                        PairAssignmentDocumentId("RealId"),
                        DateTime.now(),
                        emptyList()
                    )
                )
                val wrapper = shallow(
                    HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {})
                )
            }) exerciseAsync {
                wrapper.find<Any>(".${styles.deleteButton}").simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues.isEmpty()
                .assertIsEqualTo(true)
            reloadSpy.spyReceivedValues.isEmpty()
                .assertIsEqualTo(true)
        }
    }
}