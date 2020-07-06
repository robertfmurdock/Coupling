package com.zegreatrob.coupling.client.player

import com.zegreatrob.coupling.client.StubDispatchFunc
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.external.reactrouter.PromptComponent
import com.zegreatrob.coupling.client.external.shallow
import com.zegreatrob.coupling.client.external.simulateInputChange
import com.zegreatrob.coupling.client.external.w3c.WindowFunctions
import com.zegreatrob.coupling.model.player.Badge
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.model.tribe.Tribe
import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minspy.SpyData
import com.zegreatrob.minspy.spyFunction
import com.zegreatrob.testmints.invoke
import com.zegreatrob.testmints.setup
import org.w3c.dom.Window
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
        shallow(
            PlayerConfigEditor,
            PlayerConfigEditorProps(tribe, player, {}, {}, StubDispatchFunc())
        )
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
        shallow(
            PlayerConfigEditor,
            PlayerConfigEditorProps(tribe, player, {}, {}, StubDispatchFunc())
        )
    } verify { wrapper ->
        wrapper.find<Any>("select[name='badge'][value='${Badge.Alternate.value}']")
            .length
            .assertIsEqualTo(1)
    }

    @Test
    fun submitWillSaveAndReload() = setup(object {
        val tribe = Tribe(TribeId("party"))
        val player = Player(id = "blarg", badge = Badge.Default.value)
        val reloaderSpy = SpyData<Unit, Unit>()

        val stubDispatchFunc = StubDispatchFunc<PlayerConfigDispatcher>()
        val wrapper = shallow(
            PlayerConfigEditor,
            PlayerConfigEditorProps(tribe, player, {}, reloaderSpy::spyFunction, stubDispatchFunc)
        )
    }) exercise {
        wrapper.simulateInputChange("name", "nonsense")
        wrapper.find<Any>("form")
            .simulate("submit", json("preventDefault" to {}))
        stubDispatchFunc.simulateSuccess<SavePlayerCommand>()
    } verify {
        stubDispatchFunc.commandsDispatched<SavePlayerCommand>()
            .assertIsEqualTo(
                listOf(SavePlayerCommand(tribe.id, player.copy(name = "nonsense")))
            )
        reloaderSpy.callCount.assertIsEqualTo(1)
    }

    @Test
    fun clickingDeleteWhenConfirmedWillRemoveAndRerouteToCurrentPairAssignments() = setup(object {
        val windowFuncs = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { true }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val stubDispatchFunc = StubDispatchFunc<PlayerConfigDispatcher>()
        val wrapper = shallow(
            playerConfigEditor(windowFuncs),
            PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction, {}, stubDispatchFunc)
        )
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}")
            .simulate("click")
        stubDispatchFunc.simulateSuccess<DeletePlayerCommand>()
    } verify {
        stubDispatchFunc.commandsDispatched<DeletePlayerCommand>()
            .assertIsEqualTo(
                listOf(DeletePlayerCommand(tribe.id, player.id!!))
            )

        pathSetterSpy.spyReceivedValues.contains(
            "/${tribe.id.value}/pairAssignments/current/"
        )
    }

    @Test
    fun clickingDeleteWhenNotConfirmedWillDoNothing() = setup(object {
        val windowFunctions = object : WindowFunctions {
            override val window: Window get() = json("confirm" to { false }).unsafeCast<Window>()
        }
        val pathSetterSpy = SpyData<String, Unit>()
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)

        val stubDispatchFunc = StubDispatchFunc<PlayerConfigDispatcher>()
        val wrapper = shallow(
            playerConfigEditor(windowFunctions),
            PlayerConfigEditorProps(tribe, player, pathSetterSpy::spyFunction, {}, stubDispatchFunc)
        )
    }) exercise {
        wrapper.find<Any>(".${styles["deleteButton"]}")
            .simulate("click")
    } verify {
        stubDispatchFunc.dispatchList.isEmpty().assertIsEqualTo(true)
    }

    @Test
    fun whenThePlayerIsModifiedLocationChangeWillPromptTheUserToSave() = setup(object {
        val tribe = Tribe(TribeId("party"))
        val player = Player("blarg", badge = Badge.Alternate.value)
        val wrapper = shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, StubDispatchFunc()))
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
        shallow(PlayerConfigEditor, PlayerConfigEditorProps(tribe, player, {}, {}, StubDispatchFunc()))
    } verify { wrapper ->
        wrapper.find(PromptComponent).props().`when`
            .assertIsEqualTo(false)
    }

}
