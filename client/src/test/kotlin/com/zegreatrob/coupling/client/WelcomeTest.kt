package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.pairassignments.assertNotNull
import com.zegreatrob.coupling.client.player.playerCard
import com.zegreatrob.coupling.client.welcome.RandomProvider
import com.zegreatrob.coupling.client.welcome.Welcome
import com.zegreatrob.coupling.components.pngPath
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minenzyme.ShallowWrapper
import com.zegreatrob.minenzyme.dataprops
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class WelcomeTest {

    private val styles = useStyles("Welcome")

    @Test
    fun whenZeroIsRolledWillShowHobbits() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 0
        }
    }) exercise {
        render(Welcome(randomProvider).create())
    } verify {
        screen.queryByText("Frodo")
            .assertNotNull()
        screen.queryByText("Sam")
            .assertNotNull()
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/frodo"), pngPath("players/samwise")))
        screen.queryByText("Together, climb mountains.")
            .assertNotNull()
    }

    @Test
    fun whenOneIsRolledWillShowTheDynamicDuo() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 1
        }
    }) exercise {
        render(Welcome(randomProvider).create())
    } verify {
        screen.queryByText("Batman")
            .assertNotNull()
        screen.queryByText("Robin")
            .assertNotNull()
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/grayson"), pngPath("players/wayne")))
        screen.queryByText("Clean up the city, together.")
            .assertNotNull()
    }

    @Test
    fun whenTwoIsRolledWillShowTheHeroesOfWWII() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 2
        }
    }) exercise {
        render(Welcome(randomProvider).create())
    } verify {
        screen.queryByText("Rosie")
            .assertNotNull()
        screen.queryByText("Wendy")
            .assertNotNull()
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/rosie"), pngPath("players/wendy")))
        screen.queryByText("Team up. Get things done.")
            .assertNotNull()
    }

    private fun ShallowWrapper<dynamic>.welcomeProverb() = find<Any>(".${styles["welcomeProverb"]}")

    private fun ShallowWrapper<dynamic>.leftCard() = find(playerCard).map { it.dataprops() }[0]

    private fun ShallowWrapper<dynamic>.rightCard() = find(playerCard).map { it.dataprops() }[1]
}
