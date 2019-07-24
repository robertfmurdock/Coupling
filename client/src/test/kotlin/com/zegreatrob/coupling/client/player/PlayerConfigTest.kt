package com.zegreatrob.coupling.client.player

import Spy
import SpyData
import com.zegreatrob.coupling.client.Badge
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.common.entity.player.Player
import com.zegreatrob.coupling.common.entity.tribe.KtTribe
import com.zegreatrob.coupling.common.entity.tribe.TribeId
import com.zegreatrob.coupling.common.toPlayer
import com.zegreatrob.minassert.assertContains
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.setupAsync
import com.zegreatrob.testmints.async.testAsync
import com.zegreatrob.testmints.setup
import kotlinext.js.jsObject
import kotlinx.coroutines.withContext
import org.w3c.dom.Window
import shallow
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json
import kotlin.test.BeforeTest
import kotlin.test.Test

class PlayerConfigTest {

    var coupling: dynamic = null
    lateinit var saveSpy: Spy<Pair<Json, String>, Promise<Unit>>
    lateinit var removeSpy: Spy<Pair<Json, String>, Promise<Unit>>

    @BeforeTest
    fun before() {
        saveSpy = object : Spy<Pair<Json, String>, Promise<Unit>> by SpyData() {}
        saveSpy.spyWillReturn(Promise.resolve(Unit))
        removeSpy = object : Spy<Pair<Json, String>, Promise<Unit>> by SpyData() {}
        removeSpy.spyWillReturn(Promise.resolve(Unit))

        coupling = jsObject<dynamic> {
            savePlayer = { player: Json, tribeId: String -> saveSpy.spyFunction(player to tribeId) }
            removePlayer = { player: Json, tribeId: String -> removeSpy.spyFunction(player to tribeId) }
        }
    }

    @Test
    fun whenTheGivenPlayerHasNoBadgeWillUseTheDefaultBadge() = setup(object : PlayerConfigBuilder {
        val tribe = KtTribe(id = TribeId("party"), name = "Party tribe", badgesEnabled = true)

        val player = Player(id = "blarg")
    }) exercise {
        shallow(PlayerConfigProps(tribe, player, listOf(player), {}, coupling, {}))
    } verify { wrapper ->
        wrapper.find("input[name='badge'][value='${Badge.Default.value}'][checked]")
                .length
                .assertIsEqualTo(1)
    }

    @Test
    fun whenTheGivenPlayerHasAltBadgeWillNotModifyPlayer() = setup(object : PlayerConfigBuilder {
        val tribe = KtTribe(id = TribeId("party"), name = "Party tribe", badgesEnabled = true)

        val player = Player(id = "blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigProps(tribe, player, listOf(player), {}, coupling, {}))
    } verify { wrapper ->
        wrapper.find("input[name='badge'][value='${Badge.Alternate.value}'][checked]")
                .length
                .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object : PlayerConfigBuilder {
                override fun buildScope() = this@withContext

                val tribe = KtTribe(TribeId("party"))
                val player = Player(id = "blarg", badge = Badge.Default.value)
                val reloaderSpy = object : Spy<Unit, Unit> by SpyData() {}

                val wrapper = shallow(PlayerConfigProps(tribe, player, listOf(player), {}, coupling, {
                    reloaderSpy.spyFunction(Unit)
                }))
            }) {
                reloaderSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find("input[name='name']")
                        .simulate(
                                "change",
                                json(
                                        "target" to json("name" to "name", "value" to "nonsense"),
                                        "persist" to {}
                                )
                        )
                wrapper.find("form")
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
            setupAsync(object : PlayerConfigBuilder {
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe = KtTribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)

                val wrapper = shallow(PlayerConfigProps(
                        tribe,
                        player,
                        listOf(player),
                        pathSetterSpy::spyFunction,
                        coupling
                ) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find(".delete-button")
                        .simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues
                    .map { it.first.toPlayer() to TribeId(it.second) }
                    .assertContains(
                            player to tribe.id
                    )
            pathSetterSpy.spyReceivedValues.contains(
                    "/${tribe.id.value}/pairAssignments/current/"
            )
        }
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = testAsync {
        withContext(this.coroutineContext) {
            setupAsync(object : PlayerConfigBuilder {
                override fun buildScope() = this@withContext
                override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()

                val pathSetterSpy = object : Spy<String, Unit> by SpyData() {}
                val tribe = KtTribe(TribeId("party"))
                val player = Player("blarg", badge = Badge.Alternate.value)
                val wrapper = shallow(PlayerConfigProps(
                        tribe,
                        player,
                        listOf(player),
                        pathSetterSpy::spyFunction,
                        coupling
                ) {})
            }) {
                pathSetterSpy.spyWillReturn(Unit)
            } exerciseAsync {
                wrapper.find(".delete-button")
                        .simulate("click")
            }
        } verifyAsync {
            removeSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
            pathSetterSpy.spyReceivedValues.isEmpty().assertIsEqualTo(true)
        }
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object : PlayerConfigBuilder {
        val tribe = KtTribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfigProps(
                tribe,
                player,
                listOf(player),
                {},
                coupling
        ) {})
    }) exercise {
        wrapper.find("input[name='name']")
                .simulate("change", json(
                        "target" to json("name" to "name", "value" to "differentName"), "persist" to {})
                )
        wrapper.update()
    } verify {
        wrapper.find(PromptComponent).props().`when`
                .assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsNotModifiedLocationChangeWillNotPromptTheUserToSave() = setup(object : PlayerConfigBuilder {
        val tribe = KtTribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
    }) exercise {
        shallow(PlayerConfigProps(
                tribe,
                player,
                listOf(player),
                {},
                coupling
        ) {})
    } verify { wrapper ->
        wrapper.find(PromptComponent).props().`when`
                .assertIsEqualTo(false)
    }

}