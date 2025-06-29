package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import emotion.react.css
import kotlinx.browser.localStorage
import react.PropsWithChildren
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState
import web.cssom.Color
import web.cssom.Display
import web.cssom.FontWeight
import web.cssom.NamedColor
import web.cssom.Padding
import web.cssom.Position
import web.cssom.em
import web.cssom.integer
import web.cssom.px
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

external interface ReminderProps : PropsWithChildren {
    var id: String
}

@ReactFunc
val Reminder by nfc<ReminderProps> { props ->
    var (lastTimeSeen, setLastTimeSeen) = useState<Instant?>(null)
    useEffect(props) {
        setLastTimeSeen(lastTimeReminderShown(props.id))
    }

    val onClose = {
        Clock.System.now()
            .also { now -> localStorage.setItem(props.id.localStorageKey(), "$now") }
            .let(setLastTimeSeen::invoke)
    }
    if (lastTimeSeen == null || (lastTimeSeen.timeSinceThen() > 14.days)) {
        div {
            css {
                background = Color("#333333eb")
                color = NamedColor.white
                fontWeight = FontWeight.bold
                padding = Padding(4.px, 8.px)
                borderRadius = 20.px
                zIndex = integer(200)
                display = Display.inlineBlock
                position = Position.relative
            }
            div {
                css {
                    position = Position.absolute
                    right = 1.em
                }
                CloseButton { this.onClose = onClose }
            }
            div {
                css {
                    paddingTop = 1.em
                    paddingBottom = 0.5.em
                }
                +props.children
            }
        }
    }
}

private fun Instant.timeSinceThen(): Duration = Clock.System.now() - this

private fun lastTimeReminderShown(reminderId: String): Instant? = localStorage.getItem(reminderId.localStorageKey())
    ?.let(Instant::parse)

private fun String.localStorageKey(): String = "coupling:reminder:$this"
