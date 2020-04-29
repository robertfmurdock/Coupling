package com.zegreatrob.coupling.client

import ShallowWrapper
import com.zegreatrob.coupling.client.external.react.get
import com.zegreatrob.coupling.client.external.react.useStyles
import com.zegreatrob.coupling.client.player.PlayerCardProps
import com.zegreatrob.coupling.client.user.GoogleSignIn
import com.zegreatrob.coupling.client.welcome.RandomProvider
import com.zegreatrob.coupling.client.welcome.Welcome
import com.zegreatrob.coupling.client.welcome.WelcomeProps
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.coupling.sdk.Sdk
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.async.ScopeMint
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.testmints.setup
import shallow
import kotlin.test.Test

class WelcomeTest {

    private val styles = useStyles("Welcome")

    @Test
    fun doesNotShowInitially() = setup(object {
    }) exercise {
        shallow(Welcome, WelcomeProps({ {} }))
    } verify { wrapper ->
        wrapper.find<Any>(".${styles.className}")
            .hasClass(styles["hidden"])
            .assertIsEqualTo(true)
    }

    @Test
    fun willShowAfterZeroTimeoutSoThatAnimationWorks() = asyncSetup(object : ScopeMint() {
        val dispatcher = object : GoogleSignIn {
            override val sdk: Sdk get() = TODO("Not yet implemented")
        }
        val props = WelcomeProps(commandFunc = dispatcher.buildCommandFunc(exerciseScope))
    }) exercise {
        shallow(Welcome, props)
    } verify { wrapper ->
        wrapper.update()
            .find<ShallowWrapper<Any>>(".${styles.className}")
            .hasClass(styles["hidden"])
            .assertIsEqualTo(false)
    }

    @Test
    fun whenZeroIsRolledWillShowHobbits() = setup(object {
        val randomProvider = object : RandomProvider {
            override fun nextRandomInt(until: Int) = 0
        }
    }) exercise {
        shallow(Welcome, WelcomeProps({ {} }, randomProvider))
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
        shallow(Welcome, WelcomeProps({ {} }, randomProvider))
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
        shallow(Welcome, WelcomeProps({ {} }, randomProvider))
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