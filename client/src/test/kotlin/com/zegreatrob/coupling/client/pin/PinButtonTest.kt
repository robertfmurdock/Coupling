package com.zegreatrob.coupling.client.pin

import ShallowWrapper
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.testmints.setup
import shallow
import kotlin.test.Test

class PinButtonTest {

    @Test
    fun whenGivenPinWithSimpleIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "angry")
    }) exercise {
        shallow(PinButton, PinButtonProps(pin))
    } verify { wrapper ->
        wrapper.assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithAlreadyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "fa-angry")
    }) exercise {
        shallow(PinButton, PinButtonProps(pin))
    } verify { wrapper ->
        wrapper.assertIconHasClasses("fa", "fa-angry")
    }

    @Test
    fun whenGivenPinWithFullyDecoratedIconWillUseStandardFontAwesomeTag() = setup(object {
        val pin = Pin(icon = "far fa-angry")
    }) exercise {
        shallow(PinButton, PinButtonProps(pin))
    } verify { wrapper ->
        wrapper.assertIconHasClasses("far", "fa-angry")
            .hasClass("fa").assertIsEqualTo(false, "should not have fa")
    }

    private fun ShallowWrapper<dynamic>.assertIconHasClasses(prefixClass: String, iconClass: String) = find<String>("i")
        .apply {
            hasClass(prefixClass).assertIsEqualTo(true, "Did not have class $prefixClass")
            hasClass(iconClass).assertIsEqualTo(true, "Did not have class $iconClass")
        }

}