package com.zegreatrob.coupling.client.player

import Spy
import SpyData
import com.benasher44.uuid.Uuid
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
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import kotlinx.coroutines.asDeferred
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
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
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() =
        setup(object {
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
    fun submitWillSaveAndReload() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object {
                val dispatcher = object : PlayerConfigDispatcher {
                    override val playerRepository get() = throw NotImplementedError("stubbed")
                    override val traceId: Uuid? get() = null
                    val saveSpy = object : Spy<Pair<Json, String>, Promise<Unit>> by SpyData() {}
                    override suspend fun TribeIdPlayer.save() {
                        saveSpy.spyFunction(player.toJson() to tribeId.value).await()
                    }
                }
                val tribe = Tribe(TribeId("party"))
                val player = Player(id = "blarg", badge = Badge.Default.value)
                val reloaderSpy = object : Spy<Unit, Unit> by SpyData() {}

                val wrapper = shallow(
                    PlayerConfigEditor,
                    PlayerConfigEditorProps(
                        tribe,
                        player,
                        {},
                        { reloaderSpy.spyFunction(Unit) },
                        dispatcher.buildCommandFunc(this@withContext)
                    )
                )
            }) {
                dispatcher.saveSpy.spyWillReturn(Promise.resolve(Unit))
                reloaderSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.simulateInputChange("name", "nonsense")
                wrapper.find<Any>("form")
                    .simulate("submit", json("preventDefault" to {}))
            }
        } verifyAsync {
            dispatcher.saveSpy.spyReceivedValues
                .map { it.first.toPlayer() to TribeId(it.second) }
                .assertContains(
                    player.copy(name = "nonsense") to tribe.id
                )
            reloaderSpy.spyReceivedValues.size.assertIsEqualTo(1)
        }
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object : WindowFunctions {
                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val dispatcher = object : PlayerConfigDispatcher {
                    override val playerRepository get() = throw NotImplementedError("stubbed")
                    override val traceId: Uuid? get() = null
                    val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                    override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                        removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                        return true
                    }
                }

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe = Tribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)

                val wrapper = shallow(
                    PlayerConfigEditorComponent(this),
                    PlayerConfigEditorProps(
                        tribe,
                        player,
                        pathSetterSpy::spyFunction,
                        {},
                        dispatcher.buildCommandFunc(this@withContext)
                    )
                )
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                dispatcher.removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles["deleteButton"]}")
                    .simulate("click")
            }
        } verifyAsync {
            dispatcher.removeSpy.spyReceivedValues
                .map { TribeId(it.first) to it.second }
                .assertContains(
                    tribe.id to player.id
                )
            pathSetterSpy.spyReceivedValues.contains(
                "/${tribe.id.value}/pairAssignments/current/"
            )
        }
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object {
                val windowFunctions = object: WindowFunctions {
                    override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
                }
                val dispatcher = object : PlayerConfigDispatcher {
                    override val playerRepository get() = throw NotImplementedError("stubbed")
                    override val traceId: Uuid? get() = null
                    val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                    override suspend fun TribeIdPlayerId.deletePlayer() = true.also {
                        removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                    }
                }
                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe = Tribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)
                val wrapper = shallow(
                    PlayerConfigEditorComponent(windowFunctions),
                    PlayerConfigEditorProps(
                        tribe,
                        player,
                        pathSetterSpy::spyFunction,
                        {},
                        dispatcher.buildCommandFunc(this@withContext)
                    )
                )
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                dispatcher.removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles["deleteButton"]}")
                    .simulate("click")
            }
        } verifyAsync {
            dispatcher.removeSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
            pathSetterSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
        }
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
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() =
        setup(object {
            val tribe = Tribe(TribeId("party"))
            val player = Player("blarg", badge = Badge.Alternate.value)
        }) exercise {
            shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, { {} }))
        } verify { wrapper ->
            wrapper.find(PromptComponent).props().`when`
                .assertIsEqualTo(false)
        }

}