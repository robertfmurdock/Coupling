package com.zegreatrob.coupling.client.player

import Spy
import SpyData
import com.zegreatrob.coupling.client.external.react.PropsClassProvider
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.client.external.react.provider
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.json.toJson
import com.zegreatrob.coupling.json.toPlayer
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.player.TribeIdPlayer
import com.zegreatrob.coupling.model.player.TribeIdPlayerId
import com.zegreatrob.coupling.model.tribe.KtTribe
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
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.Test

class PlayerConfigTest {

    private val styles = loadStyles<PlayerConfigStyles>("player/PlayerConfig")

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object : PlayerConfigRenderer,
        PropsClassProvider<PlayerConfigProps> by provider() {
        override val playerRepository get() = throw NotImplementedError("stubbed")
        val tribe = KtTribe(
            id = TribeId("party"),
            name = "Party tribe",
            badgesEnabled = true
        )

        val player = Player(id = "blarg")
    }) exercise {
        shallow(PlayerConfigProps(tribe, player, listOf(player), {}, {}))
    } verify { wrapper ->
        wrapper.find<Any>("input[name='badge'][value='${Badge.Default.value}'][checked]")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object : PlayerConfigRenderer,
        PropsClassProvider<PlayerConfigProps> by provider() {
        override val playerRepository get() = throw NotImplementedError("stubbed")
        val tribe = KtTribe(
            id = TribeId("party"),
            name = "Party tribe",
            badgesEnabled = true
        )

        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigProps(tribe, player, listOf(player), {}, {}))
    } verify { wrapper ->
        wrapper.find<Any>("input[name='badge'][value='${Badge.Alternate.value}'][checked]")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object : PlayerConfigRenderer,
                PropsClassProvider<PlayerConfigProps> by provider() {
                override val playerRepository get() = throw NotImplementedError("stubbed")
                override fun buildScope() = this@withContext

                val saveSpy = object : Spy<Pair<Json, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayer.save() {
                    saveSpy.spyFunction(player.toJson() to tribeId.value).await()
                }

                val tribe =
                    KtTribe(TribeId("party"))
                val player = Player(id = "blarg", badge = Badge.Default.value)
                val reloaderSpy = object : Spy<Unit, Unit> by SpyData() {}

                val wrapper = shallow(PlayerConfigProps(tribe, player, listOf(player), {}, {
                    reloaderSpy.spyFunction(Unit)
                }))
            }) {
                saveSpy.spyWillReturn(Promise.resolve(Unit))
                reloaderSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find<Any>("input[name='name']")
                    .simulate(
                        "change",
                        json(
                            "target" to json("name" to "name", "value" to "nonsense"),
                            "persist" to {}
                        )
                    )
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
            setupAsync(object : PlayerConfigRenderer,
                PropsClassProvider<PlayerConfigProps> by provider() {
                override val playerRepository get() = throw NotImplementedError("stubbed")
                override fun buildScope() = this@withContext
                val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                    removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                    return true
                }

                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe =
                    KtTribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)

                val wrapper = shallow(PlayerConfigProps(
                    tribe,
                    player,
                    listOf(player),
                    pathSetterSpy::spyFunction
                ) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles.deleteButton}")
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
            setupAsync(object : PlayerConfigRenderer,
                PropsClassProvider<PlayerConfigProps> by provider() {
                override val playerRepository get() = throw NotImplementedError("stubbed")
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
                val removeSpy = object : Spy<Pair<String, String>, Promise<Unit>> by SpyData() {}

                override suspend fun TribeIdPlayerId.deletePlayer(): Boolean {
                    removeSpy.spyFunction(tribeId.value to playerId).asDeferred().await()
                    return true
                }

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe =
                    KtTribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)
                val wrapper = shallow(PlayerConfigProps(
                    tribe,
                    player,
                    listOf(player),
                    pathSetterSpy::spyFunction
                ) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
                removeSpy.spyWillReturn(Promise.resolve(Unit))
            } exerciseAsync {
                wrapper.find<Any>(".${styles.deleteButton}")
                    .simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
            pathSetterSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
        }
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object : PlayerConfigRenderer,
        PropsClassProvider<PlayerConfigProps> by provider() {
        override val playerRepository get() = throw NotImplementedError("stubbed")
        val tribe = KtTribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfigProps(
            tribe,
            player,
            listOf(player),
            {}
        ) {})
    }) exercise {
        wrapper.find<Any>("input[name='name']")
            .simulate(
                "change", json(
                    "target" to json("name" to "name", "value" to "differentName"), "persist" to {})
            )
        wrapper.update()
    } verify {
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = setup(object : PlayerConfigRenderer,
        PropsClassProvider<PlayerConfigProps> by provider() {
        override val playerRepository get() = throw NotImplementedError("stubbed")
        val tribe = KtTribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigProps(
            tribe,
            player,
            listOf(player),
            {}
        ) {})
    } verify { wrapper ->
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(false)
    }

}