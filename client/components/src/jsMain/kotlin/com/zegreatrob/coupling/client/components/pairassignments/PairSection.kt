package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.external.html2canvas.html2canvas
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.player.TinyPlayerList
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.PartyDetails
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import react.ChildrenBuilder
import react.MutableRefObject
import react.Props
import react.dom.html.ButtonHTMLAttributes
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useRef
import web.cssom.ClassName
import web.cssom.Display
import web.cssom.Float
import web.cssom.VerticalAlign
import web.cssom.px
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

external interface PairSectionProps : Props {
    var party: PartyDetails
    var players: List<Player>
    var pairAssignments: PairAssignmentDocument?
    var allowSave: Boolean
    var setPairs: (PairAssignmentDocument) -> Unit
    var controls: Controls<DeletePairAssignmentsCommand.Dispatcher>
}

@ReactFunc
val PairSection by nfc<PairSectionProps> { (party, players, pairs, allowSave, setPairs, controls) ->
    val pairSectionNode = useRef<HTMLElement>(null)

    div {
        css { verticalAlign = VerticalAlign.top }
        div {
            ref = pairSectionNode
            css { display = Display.inlineBlock }
            PairSectionPanel(party, players, pairs, allowSave, setPairs, controls)
        }
        div {
            css { float = Float.right; width = 0.px }
            div { copyToClipboardButton(pairSectionNode) }
            TinyPlayerList(party, players)
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<HTMLElement>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        CouplingButton(
            sizeRuleSet = large,
            colorRuleSet = white,
            onClick = { ref.current?.copyToClipboardOnClick()?.invoke() },
            attrs = fun ButtonHTMLAttributes<*>.() { tabIndex = -1 },
        ) {
            i { className = ClassName("fa fa-clipboard") }
        }
    }
}

private fun HTMLElement.copyToClipboardOnClick(): () -> Unit = writeImageToClipboardAsPromise()

private fun HTMLElement.writeImageToClipboardAsPromise(): () -> Unit = {
    window.navigator.clipboard.write(
        dataTransfer(
            html2canvas(this, json("useCORS" to true, "imageTimeout" to 500))
                .then { Promise { resolve, _ -> it.toBlob(resolve, "image/svg") } },
        ),
    )
}

private fun dataTransfer(it: Any) = arrayOf(ClipboardItem(json("image/png" to it))).unsafeCast<DataTransfer>()

external class ClipboardItem(params: Json)
