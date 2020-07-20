package com.zegreatrob.coupling.client

import com.zegreatrob.minenzyme.external.ShallowWrapper
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.minenzyme.external.shallow
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.welcome.RandomProvider
import com.zegreatrob.coupling.client.welcome.Welcome
import com.zegreatrob.coupling.client.welcome.WelcomeProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.invoke
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
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.findLeftCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Frodo",
                    name = "Frodo",
                    imageURL = "/images/icons/players/frodo-icon.png"
                )
            )
        wrapper.findRightCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Sam",
                    name = "Sam",
                    imageURL = "/images/icons/players/samwise-icon.png"
                )
            )
        wrapper.find<Any>(".${styles["welcomeProverb"]}")
            .text()
            .assertIsEqualTo("Together, climb mountains.")
    }

    @Test
    fun whenOneIsRolledWillShowTheDynamicDuo() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 1
        }
    }) exercise {
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.findLeftCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Batman",
                    name = "Batman",
                    imageURL = "/images/icons/players/grayson-icon.png"
                )
            )
        wrapper.findRightCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Robin",
                    name = "Robin",
                    imageURL = "/images/icons/players/wayne-icon.png"
                )
            )
        wrapper.find<Any>(".${styles["welcomeProverb"]}")
            .text()
            .assertIsEqualTo("Clean up the city, together.")
    }

    @Test
    fun whenTwoIsRolledWillShowTheHeroesOfWWII() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 2
        }
    }) exercise {
        shallow(Welcome, WelcomeProps(StubDispatchFunc(), randomProvider))
    } verify { wrapper ->
        wrapper.findLeftCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Rosie",
                    name = "Rosie",
                    imageURL = "/images/icons/players/rosie-icon.png"
                )
            )
        wrapper.findRightCardProps()
            .player
            .assertIsEqualTo(
                Player(
                    id = "Wendy",
                    name = "Wendy",
                    imageURL = "/images/icons/players/wendy-icon.png"
                )
            )
        wrapper.find<Any>(".${styles["welcomeProverb"]}")
            .text()
            .assertIsEqualTo("Team up. Get things done.")
    }

    private fun ShallowWrapper<dynamic>.findRightCardProps() = find<PlayerCardProps>(".${styles["playerCard"]}.right")
        .props()

    private fun ShallowWrapper<dynamic>.findLeftCardProps() = find<PlayerCardProps>(".${styles["playerCard"]}.left")
        .props()

}
