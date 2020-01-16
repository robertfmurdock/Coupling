package com.zegreatrob.coupling.client.pin

import ShallowWrapper
import com.zegreatrob.coupling.client.external.react.loadStyles
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import findByClass
import shallow
import kotlin.test.Test

class PinCardTest {

    private val styles = loadStyles<PinCardStyles>("pin/PinCard")

    @Test
    fun whenGivenPinWithSimpleIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "angry")
    }) exercise {
        shallow(PinCard, PinCardProps(pin))
    } verify { wrapper ->
        wrapper.findByClass(styles.icon)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithAlreadyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "fa-angry")
    }) exercise {
        shallow(PinCard, PinCardProps(pin))
    } verify { wrapper ->
        wrapper.findByClass(styles.icon)
            .assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithFullyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "far fa-angry")
    }) exercise {
        shallow(PinCard, PinCardProps(pin))
    } verify { wrapper ->
        wrapper.findByClass(styles.icon)
            .assertIconHasClasses("far", "fa-angry")
            .hasClass("fa").assertIsEqualTo(false, "should not have fa")
    }

    private fun ShallowWrapper<dynamic>.assertIconHasClasses(prefixClass: String, iconClass: String) = find<String>("i")
        .apply {
            hasClass(prefixClass).assertIsEqualTo(true, "Did not have class $prefixClass")
            hasClass(iconClass).assertIsEqualTo(true, "Did not have class $iconClass")
        }

}