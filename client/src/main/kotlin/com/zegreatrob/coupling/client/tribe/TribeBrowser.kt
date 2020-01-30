package com.zegreatrob.coupling.client.tribe

import com.zegreatrob.coupling.client.ConfigHeader.configHeader
import com.zegreatrob.coupling.client.external.react.*
import com.zegreatrob.coupling.model.tribe.Tribe
import kotlinx.html.classes
import react.RBuilder
import react.RProps
import react.dom.a
import react.dom.div
import react.dom.i
import react.dom.span

data class TribeBrowserProps(val tribe: Tribe, val pathSetter: (String) -> Unit) : RProps

object TribeBrowser : FRComponent<TribeBrowserProps>(provider()) {

    val styles = useStyles("tribe/TribeBrowser")

    fun RBuilder.tribeBrowser(tribe: Tribe, pathSetter: (String) -> Unit) =
        tribeBrowser(TribeBrowserProps(tribe, pathSetter))

    override fun render(props: TribeBrowserProps) = reactElement {
        val (tribe, pathSetter) = props

        div(styles.className) {
            configHeader(tribe, pathSetter) {
                span(styles["headerContents"]) {
                    span(styles["headerText"]) { +(tribe.name ?: "") }
                    tribeControlButtons()
                }
            }
        }
    }

    private fun RBuilder.tribeControlButtons() = span(classes = styles["controlButtons"]) {
        tribeSelectButton()
        logoutButton()
    }

    private fun RBuilder.logoutButton() = a(href = "/logout", classes = "large red button") {
        attrs { classes += styles["logoutButton"] }
        i(classes = "fa fa-sign-out") {}
        span { +"Sign Out" }
    }

    private fun RBuilder.tribeSelectButton() = a(href = "/tribes/", classes = "large gray button") {
        attrs { classes += styles["tribeSelectButton"] }
        i(classes = "fa fa-arrow-circle-up") {}
        span { +"Tribe select" }
    }

}

private val RBuilder.tribeBrowser: RenderToBuilder<TribeBrowserProps>
    get() {
        return TribeBrowser.render(this)
    }
