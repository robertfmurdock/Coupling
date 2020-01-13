package com.zegreatrob.coupling.client.pin

import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.client.routing.PageProps
import com.zegreatrob.coupling.client.routing.ReloadFunction
import com.zegreatrob.coupling.client.routing.dataLoadProps
import com.zegreatrob.coupling.client.routing.dataLoadWrapper
import com.zegreatrob.coupling.sdk.SdkSingleton
import com.zegreatrob.coupling.sdk.RepositoryCatalog
import com.zegreatrob.coupling.model.pin.Pin
import com.zegreatrob.coupling.model.tribe.Tribe
import react.RBuilder
import react.ReactElement

object PinPage : RComponent<PageProps>(provider()), PinPageBuilder,
    RepositoryCatalog by SdkSingleton

private val LoadedPin = dataLoadWrapper(PinConfig)
private val RBuilder.loadedPin get() = LoadedPin.render(this)

interface PinPageBuilder : SimpleComponentRenderer<PageProps>, TribePinQueryDispatcher {

    override fun RContext<PageProps>.render(): ReactElement {
        val tribeId = props.tribeId
        val pinId = props.pinId

        return if (tribeId != null) {
            reactElement {
                loadedPin(
                    dataLoadProps(
                        query = { TribePinQuery(tribeId, pinId).perform() },
                        toProps = toPropsFunc(props)
                    )
                ) {
                    pinId?.let { attrs { key = it } }
                }
            }
        } else throw Exception("WHAT")
    }

    private fun toPropsFunc(pageProps: PageProps): (ReloadFunction, Triple<Tribe?, List<Pin>, Pin>) -> PinConfigProps =
        { reload, (tribe, pins, pin) ->
            PinConfigProps(
                tribe = tribe!!,
                pin = pin,
                pins = pins,
                pathSetter = pageProps.pathSetter,
                reload = reload
            )
        }
}
