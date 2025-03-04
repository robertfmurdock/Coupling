package com.zegreatrob.coupling.client.components

import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import kotlinx.browser.localStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import react.PropsWithChildren
import react.useEffect
import react.useState
import kotlin.time.Duration.Companion.days

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
        val now = Clock.System.now()
        localStorage.setItem(props.id.localStorageKey(), "$now")
        setLastTimeSeen(now)
    }
    if (lastTimeSeen == null || (Clock.System.now() - lastTimeSeen > 14.days)) {
        CloseButton { this.onClose = onClose }

        +props.children
    }
}

private fun lastTimeReminderShown(reminderId: String): Instant? = localStorage.getItem(reminderId.localStorageKey())?.let { Instant.parse(it) }

private fun String.localStorageKey(): String = "reminder-$this"
