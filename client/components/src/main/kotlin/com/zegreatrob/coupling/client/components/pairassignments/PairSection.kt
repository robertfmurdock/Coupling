package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.action.pairassignmentdocument.DeletePairAssignmentsCommand
import com.zegreatrob.coupling.client.components.Controls
import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.external.html2canvas.html2canvas
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.player.TinyPlayerList
import com.zegreatrob.coupling.client.components.white
import com.zegreatrob.coupling.model.pairassignmentdocument.PairAssignmentDocument
import com.zegreatrob.coupling.model.party.Party
import com.zegreatrob.coupling.model.player.Player
import com.zegreatrob.minreact.DataPropsBind
import com.zegreatrob.minreact.add
import com.zegreatrob.minreact.ntmFC
import csstype.ClassName
import csstype.Display
import csstype.Float
import csstype.VerticalAlign
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import org.w3c.dom.DataTransfer
import react.ChildrenBuilder
import react.MutableRefObject
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.useRef
import web.html.HTMLElement
import kotlin.js.Json
import kotlin.js.Promise
import kotlin.js.json

data class PairSection(
    val party: Party,
    val players: List<Player>,
    val pairAssignments: PairAssignmentDocument?,
    val allowSave: Boolean,
    val setPairs: (PairAssignmentDocument) -> Unit,
    val controls: Controls<DeletePairAssignmentsCommand.Dispatcher>,
) : DataPropsBind<PairSection>(pairSection)

private val pairSection by ntmFC<PairSection> { (party, players, pairs, allowSave, setPairs, controls) ->
    val pairSectionNode = useRef<HTMLElement>(null)

    div {
        css { verticalAlign = VerticalAlign.top }
        div {
            ref = pairSectionNode
            css { display = Display.inlineBlock }
            add(PairSectionPanel(party, players, pairs, allowSave, setPairs, controls))
        }
        div {
            css { float = Float.right; width = 0.px }
            div { copyToClipboardButton(pairSectionNode) }
            add(TinyPlayerList(party, players))
        }
    }
}

private fun ChildrenBuilder.copyToClipboardButton(ref: MutableRefObject<HTMLElement>) {
    if (js("!!global.ClipboardItem").unsafeCast<Boolean>()) {
        add(
            CouplingButton(
                sizeRuleSet = large,
                colorRuleSet = white,
                onClick = { ref.current?.copyToClipboardOnClick()?.invoke() },
                attrs = { tabIndex = -1 },
            ),
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
