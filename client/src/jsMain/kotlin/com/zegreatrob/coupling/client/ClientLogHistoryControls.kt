package com.zegreatrob.coupling.client

import com.zegreatrob.coupling.client.components.CouplingButton
import com.zegreatrob.coupling.client.components.blue
import com.zegreatrob.coupling.client.components.large
import com.zegreatrob.coupling.client.components.red
import com.zegreatrob.minreact.nfc
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import react.Props
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.details
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.section
import react.dom.html.ReactHTML.summary
import react.useState
import web.aria.AriaRole
import kotlin.js.Date

val ClientLogHistoryControls by nfc<Props> {
    var status by useState(ClientLogCapture.statusSummary())
    section {
        h2 { +"Client log history" }
        p {
            role = "status".unsafeCast<AriaRole>()
            +"$status Exported logs stay on this device until you choose to share them."
        }
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = blue
            onClick = {
                downloadClientLogs()
                status = "Download started. ${ClientLogCapture.statusSummary()}"
            }
            +"Download client logs (JSONL)"
        }
        CouplingButton {
            sizeRuleSet = large
            colorRuleSet = red
            onClick = {
                ClientLogCapture.clear()
                status = ClientLogCapture.statusSummary()
            }
            +"Clear saved client logs"
        }
        details {
            summary { +"Querying the exported JSONL" }
            p { +"Each line is JSON. The first line is export coverage metadata; event lines can be filtered with jq:" }
            h3 { +"Filter by client version" }
            pre { code { +"jq 'select(.type == \"client-log-event\" and .clientVersion == \"2.5.0\")' client-logs.jsonl" } }
            h3 { +"Filter by time" }
            pre { code { +"jq 'select(.type == \"client-log-event\" and .message.timestamp >= \"2026-11-24T00:00:00Z\")' client-logs.jsonl" } }
            h3 { +"Filter by operation and outcome" }
            pre { code { +"jq 'select(.type == \"client-log-event\" and .structuredProperties.command_action == \"LoadPartyAction\" and .structuredProperties.command_phase == \"end\" and (.structuredProperties.command_error == true or .structuredProperties.command_cancelled == true))' client-logs.jsonl" } }
            h3 { +"Find correlated activity" }
            pre { code { +"jq 'select(.type == \"client-log-event\" and .structuredProperties.command_trace_id == \"TRACE_ID\")' client-logs.jsonl" } }
        }
    }
}

private fun downloadClientLogs() {
    val logJsonl = ClientLogCapture.export()
    val blobUrl = js("URL.createObjectURL(new Blob([logJsonl], { type: 'application/x-ndjson' }))") as String
    val anchor = document.createElement("a").unsafeCast<HTMLAnchorElement>()
    anchor.href = blobUrl
    anchor.download = "coupling-client-logs-${Date.now().toLong()}.jsonl"
    document.body?.appendChild(anchor)
    anchor.click()
    anchor.remove()
    window.setTimeout({ js("URL.revokeObjectURL(blobUrl)") }, 0)
}
