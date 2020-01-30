package com.zegreatrob.coupling.client.player

import Spy
import SpyData
import com.zegreatrob.coupling.client.external.react.FRComponent
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.sdk.PlayerRepository
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

    abstract class RendererWithStub : FRComponent<PlayerConfigEditorProps>(provider()), PlayerConfigEditorRenderer {
        override val playerRepository: PlayerRepository get() = throw NotImplementedError("stubbed")
    }

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() =
        setup(object : RendererWithStub() {
            val tribe = Tribe(
                id = TribeId("party"),
                name = "Party tribe",
                badgesEnabled = true
            )
            val player = Player(id = "blarg")
        }) exercise {
            shallow(PlayerConfigEditorProps(tribe, player, {}, {}))
        } verify { wrapper ->
            wrapper.find<Any>("select[name='badge'][value='${Badge.Default.value}']")
                .length
                .assertIsEqualTo(1)
        }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object : RendererWithStub() {
        val tribe = Tribe(
            id = TribeId("party"),
            name = "Party tribe",
            badgesEnabled = true
        )
        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigEditorProps(tribe, player, {}, {}))
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object : RendererWithStub() {
                override val playerRepository get() = throw NotImplementedError("stubbed")
                override fun buildScope() = this@withContext

                val saveSpy = object : Spy<Pair<Json, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayer.save() {
                    saveSpy.spyFunction(player.toJson() to tribeId.value).await()
                }

                val tribe =
                    Tribe(TribeId("party"))
                val player = Player(id = "blarg", badge = Badge.Default.value)
                val reloaderSpy = object : Spy<Unit, Unit> by SpyData() {}

                val wrapper = shallow(PlayerConfigEditorProps(tribe, player, {}, {
                    reloaderSpy.spyFunction(Unit)
                }))
            }) {
                saveSpy.spyWillReturn(Promise.resolve(Unit))
                reloaderSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.simulateInputChange("name", "nonsense")
                wrapper.find<Any>("form")
                    .simulate("submit", json("preventDefault" to {}))
            }
        } verifyAsync {
            saveSpy.spyReceivedValues
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
            setupAsync(object : RendererWithStub() {
                override fun buildScope() = this@withContext
                val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                    removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                    return true
                }

                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe = Tribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)

                val wrapper = shallow(PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles["deleteButton"]}")
                    .simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues
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
            setupAsync(object : RendererWithStub() {
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
                val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                    removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                    return true
                }

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe =
                    Tribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)
                val wrapper = shallow(PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles["deleteButton"]}")
                    .simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
            pathSetterSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
        }
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object : RendererWithStub() {
        override val playerRepository get() = throw NotImplementedError("stubbed")
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfigEditorProps(tribe, player, {}) {})
    }) exercise {
        wrapper.simulateInputChange("name", "differentName")
        wrapper.update()
    } verify {
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() =
        setup(object : RendererWithStub() {
            override val playerRepository get() = throw NotImplementedError("stubbed")
            val tribe = Tribe(TribeId("party"))
            val player = Player("blarg", badge = Badge.Alternate.value)
        }) exercise {
            shallow(PlayerConfigEditorProps(tribe, player, {}) {})
        } verify { wrapper ->
            wrapper.find(PromptComponent).props().`when`
                .assertIsEqualTo(false)
        }

}