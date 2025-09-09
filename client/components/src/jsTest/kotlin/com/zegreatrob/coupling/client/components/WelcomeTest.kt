package com.zegreatrob.coupling.client.components

import com.zegreatrob.coupling.client.components.welcome.RandomProvider
import com.zegreatrob.coupling.client.components.welcome.Welcome
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.setup
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import kotlin.test.Test

class WelcomeTest {

    @Test
    fun whenZeroIsRolledWillShowHobbits() = setup(object : RandomProvider {
        val randomProvider = this
        override fun nextRandomInt(until: Int) = 0
    }) exercise {
        render { Welcome(randomProvider) }
    } verify {
        screen.queryByText("Frodo")
            .assertIsNotEqualTo(null)
        screen.queryByText("Sam")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("player-icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(CouplingImages.images.frodo, CouplingImages.images.samwise))
        screen.queryByText("Together, climb mountains.")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenOneIsRolledWillShowTheDynamicDuo() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 1
        }
    }) exercise {
        render { Welcome(randomProvider) }
    } verify {
        screen.queryByText("Batman")
            .assertIsNotEqualTo(null)
        screen.queryByText("Robin")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("player-icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(CouplingImages.images.grayson, CouplingImages.images.wayne))
        screen.queryByText("Clean up the city, together.")
            .assertIsNotEqualTo(null)
    }

    @Test
    fun whenTwoIsRolledWillShowTheHeroesOfWWII() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 2
        }
    }) exercise {
        render { Welcome(randomProvider) }
    } verify {
        screen.queryByText("Rosie")
            .assertIsNotEqualTo(null)
        screen.queryByText("Wendy")
            .assertIsNotEqualTo(null)
        screen.queryAllByAltText("player-icon")
            .map { it.getAttribute("src") }
            .assertIsEqualTo(listOf(CouplingImages.images.rosie, CouplingImages.images.wendy))
        screen.queryByText("Team up. Get things done.")
            .assertIsNotEqualTo(null)
    }
}
