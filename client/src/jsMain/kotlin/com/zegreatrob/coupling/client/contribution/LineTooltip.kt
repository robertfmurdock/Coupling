package com.zegreatrob.coupling.client.contribution

import com.zegreatrob.coupling.client.components.graphing.external.recharts.RechartsTooltipArgs
import emotion.react.css
import react.FC
import react.PropsWithValue
import react.dom.html.ReactHTML.div
import web.cssom.Color
import web.cssom.px

val LineTooltip = FC<PropsWithValue<RechartsTooltipArgs>> { props ->
    val args = props.value
    div {
        css {
            backgroundColor = Color("rgb(0 0 0 / 14%)")
            padding = 10.px
            borderRadius = 20.px
        }
        args.payload?.forEach { payload ->
            div {
                key = payload.name.toString()
                +"${payload.name} - ${payload.value}"
            }
        }
        div { +args.labelFormatter(args.label) }
    }
}
