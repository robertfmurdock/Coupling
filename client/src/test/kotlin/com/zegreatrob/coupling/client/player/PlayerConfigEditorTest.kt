package com.zegreatrob.coupling.client.player

import com.benasher44.uuid.uuid4
import com.zegreatrob.coupling.client.buildCommandFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.*
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import org.w3c.dom.Window
import shallow
import simulateInputChange
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigEditorTest {

    private val styles = useStyles("player/PlayerConfigEditor")

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object {
        val tribe = Tribe(
            id = TribeId("party"),
            name = "Party tribe",
            badgesEnabled = true
        )
        val player = Player(id = "blarg")
    }) exercise {
        shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, { {} }))
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Default.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object {
        val tribe = Tribe(
            id = TribeId("party"),
            name = "Party tribe",
            badgesEnabled = true
        )
        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, { {} }))
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = asyncSetup(object : ScopeMint() {
        val dispatcher = object : PlayerConfigDispatcher {
            override val playerRepository get() = throw NotImplementedError("stubbed")
            override val traceId = uuid4()
            val saveSpy = SpyData<Pair<Json, String>, Promise<Unit>>()
            override suspend fun TribeIdPlayer.save() {
                saveSpy.spyFunction(player.toJson() to tribeId.value).await()
            }
        }
        val tribe = Tribe(TribeId("party"))
        val player = Player(id = "blarg", badge = Badge.Default.value)
        val reloaderSpy = SpyData<Unit, Unit>()

        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)
        val wrapper = shallow(
            PlayerConfigEditor,
            PlayerConfigEditorProps(tribe, player, {}, { reloaderSpy.spyFunction(Unit) }, commandFunc)
        )
    }, {
        dispatcher.saveSpy.spyWillReturn(Promise.resolve(Unit))
        reloaderSpy.spyWillReturn(Unit)
    }) exercise {
        wrapper.simulateInputChange("name", "nonsense")
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
    } verify {
        dispatcher.saveSpy.spyReceivedValues
            .map { it.first.toPlayer() to TribeId(it.second) }
            .assertContains(
                player.copy(name = "nonsense") to tribe.id
            )
        reloaderSpy.spyReceivedValues.size.assertIsEqualTo(1)
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = asyncSetup(object : ScopeMint() {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val dispatcher = object : PlayerConfigDispatcher {
            override val playerRepository get() = throw NotImplementedError("stubbed")
            override val traceId = uuid4()
            val removeSpy = SpyData<Pair<String, String>, Promise<Unit>>()

            override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                return true
            }
        }
        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)

        val pathSetterSpy = SpyData<String, Unit>()
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val wrapper = shallow(
            PlayerConfigEditorComponent(windowFuncs),
            PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction, {}, commandFunc)
        )
    }, {
        pathSetterSpy.spyWillReturn(Unit)
        dispatcher.removeSpy.spyWillReturn(Promise.resolve(Unit))
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}")
            .simulate("click")
    } verify {
        dispatcher.removeSpy.spyReceivedValues
            .map { TribeId(it.first) to it.second }
            .assertContains(
                tribe.id to player.id
            )
        pathSetterSpy.spyReceivedValues.contains(
            "/${tribe.id.value}/pairAssignments/current/"
        )
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = asyncSetup(object : ScopeMint() {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val dispatcher = object : PlayerConfigDispatcher {
            override val playerRepository get() = throw NotImplementedError("stubbed")
            override val traceId = uuid4()
            val removeSpy = SpyData<Pair<String, String>, Promise<Unit>>()

            override suspend fun TribeIdPlayerId.deletePlayer() = true.also {
                removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
            }
        }
        val commandFunc = dispatcher.buildCommandFunc(exerciseScope)

        val pathSetterSpy = SpyData<String, Unit>()
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val wrapper = shallow(
            PlayerConfigEditorComponent(windowFunctions),
            PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction, {}, commandFunc)
        )
    }, {
        pathSetterSpy.spyWillReturn(Unit)
        dispatcher.removeSpy.spyWillReturn(Promise.resolve(Unit))
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}")
            .simulate("click")
    } verify {
        dispatcher.removeSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
        pathSetterSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object {
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, { {} }))
    }) exercise {
        wrapper.simulateInputChange("name", "differentName")
        wrapper.update()
    } verify {
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = setup(object {
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, { {} }))
    } verify { wrapper ->
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(false)
    }

}