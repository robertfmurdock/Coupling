package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.welcome.RandomProvider
import com.zegreatrob.coupling.client.components.welcome.Welcome
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.render
import com.zegreatrob.coupling.testreact.external.testinglibrary.react.screen
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.minreact.create
import com.zegreatrob.testmints.setup
import kotlin.test.Test

class WelcomeTest {

    @Test
    fun whenZeroIsRolledWillShowHobbits() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 0
        }
    }) exercise {
        render(Welcome(randomProvider).create())
    } verify {
        screen.queryByText("Frodo")
            .assertIsNotEqualTo(null)
        screen.queryByText("Sam")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/frodo"), pngPath("players/samwise")))
        screen.queryByText("Together, climb mountains.")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenOneIsRolledWillShowTheDynamicDuo() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 1
        }
    }) exercise {
        render(Welcome(randomProvider).create {})
    } verify {
        screen.queryByText("Batman")
            .assertIsNotEqualTo(null)
        screen.queryByText("Robin")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/grayson"), pngPath("players/wayne")))
        screen.queryByText("Clean up the city, together.")
            .assertIsNotEqualTo(null)
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
            .assertIsNotEqualTo(null)
        screen.queryByText("Wendy")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(pngPath("players/rosie"), pngPath("players/wendy")))
        screen.queryByText("Team up. Get things done.")
            .assertIsNotEqualTo(null)
    }
}
