package com.zegreatrob.coupling.client.pairassignments

import Spy
import SpyData
import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.soywiz.klock.DateTime
import com.zegreatrob.coupling.client.buildCommandFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.client.pairassignments.list.DeletePairAssignmentsCommandDispatcher
import com.zegreatrob.coupling.client.pairassignments.list.HistoryComponent
import com.zegreatrob.coupling.client.pairassignments.list.HistoryProps
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocumentId
import com.zegreatrob.coupling.model.pairassignmentdocument.TribeIdPairAssignmentDocumentId
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.setupAsync2
import org.w3c.dom.Window
import shallow
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class HistoryTest {

    private val styles = useStyles("pairassignments/History")

    private fun deleteDispatcher() = object : DeletePairAssignmentsCommandDispatcher {
        override val traceId = uuid4()
        override val pairAssignmentDocumentRepository get() = throw NotImplementedError("")
        val removeSpy = SpyData<Unit, Promise<Unit>>().also { it.spyWillReturn(Promise.resolve(Unit)) }
        override suspend fun TribeIdPairAssignmentDocumentId.delete() = removeSpy.spyFunction(Unit).let { true }
    }

    @Test
    fun whenRemoveIsCalledAndConfirmedWillDeletePlayer() = setupAsync2(object : ScopeMint(), WindowFunctions {
        val dispatcher = deleteDispatcher()
        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)
        override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

        val tribe = Tribe(TribeId("me"))

        val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

        val history = listOf(
            PairAssignmentDocument(
                PairAssignmentDocumentId("RealId"),
                DateTime.now(),
                emptyList()
            )
        )
        val wrapper = shallow(
            HistoryComponent(this),
            HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {}, commandFunc)
        )
    }) {
        dispatcher.removeSpy.spyWillReturn(Promise.resolve(Unit))
        reloadSpy.spyWillReturn(Unit)
    } exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}").simulate("click")
    } verify {
        dispatcher.removeSpy.spyReceivedValues.isNotEmpty()
            .assertIsEqualTo(true)
        reloadSpy.spyReceivedValues.isNotEmpty()
            .assertIsEqualTo(true)
    }

    @Test
    fun whenRemoveIsCalledAndNotConfirmedWillNotDeletePlayer() = setupAsync2(object : ScopeMint(), WindowFunctions {
        val dispatcher = deleteDispatcher()
        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)
        override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()

        val tribe = Tribe(TribeId("me"))

        val reloadSpy = object : Spy<Unit, Unit> by SpyData() {}

        val history = listOf(
            PairAssignmentDocument(
                PairAssignmentDocumentId("RealId"),
                DateTime.now(),
                emptyList()
            )
        )
        val wrapper = shallow(
            HistoryComponent(this),
            HistoryProps(tribe, history, { reloadSpy.spyFunction(Unit) }, {}, commandFunc)
        )
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}").simulate("click")
    } verify {
        dispatcher.removeSpy.spyReceivedValues.isEmpty()
            .assertIsEqualTo(true)
        reloadSpy.spyReceivedValues.isEmpty()
            .assertIsEqualTo(true)
    }
}