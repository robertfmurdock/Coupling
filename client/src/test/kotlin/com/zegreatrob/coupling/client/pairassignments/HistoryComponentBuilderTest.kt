package com.zegreatrob.coupling.client.pairassignments

import Spy
import SpyData
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.loadStyles
import com.zegreatrob.coupling.client.pairassignments.list.HistoryComponentBuilder
import com.zegreatrob.coupling.client.pairassignments.list.HistoryProps
import com.zegreatrob.coupling.client.pairassignments.list.HistoryStyles
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.common.entity.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.withContext
import org.w3c.dom.Window
import shallow
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class HistoryComponentBuilderTest {

    private val styles = loadStyles<HistoryStyles>("pairassignments/History")

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = testAsync {
        withContext(Dispatchers.Default) {
            setupAsync(object : HistoryComponentBuilder {
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val tribe = KtTribe(TribeId("me"))
                val removeSpy = object : Spy<Unit, Promise<Unit>> by SpyData() {}
                override fun deleteAsync(tribeId: TribeId, pairAssignmentDocId: PairAssignmentDocumentId) =
                        removeSpy.spyFunction(Unit).asDeferred()

                val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

                val history = listOf(
                        PairAssignmentDocument(DateTime.now(), emptyList(), PairAssignmentDocumentId("RealId"))
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
            setupAsync(object : HistoryComponentBuilder {
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()

                val tribe = KtTribe(TribeId("me"))
                val removeSpy = object : Spy<Unit, Promise<Unit>> by SpyData() {}
                override fun deleteAsync(tribeId: TribeId, pairAssignmentDocId: PairAssignmentDocumentId) =
                        removeSpy.spyFunction(Unit).asDeferred()

                val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

                val history = listOf(PairAssignmentDocument(
                        DateTime.now(), emptyList(), PairAssignmentDocumentId("RealId"))
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