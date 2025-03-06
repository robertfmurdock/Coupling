package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.Reminder
import react.FC
import react.dom.html.ReactHTML.h3
import react.dom.html.ReactHTML.h4
import react.dom.html.ReactHTML.p

val PinReminder = FC {
    Reminder("pins-exist") {
        h3 { +"Pins!" }
        p { +"Have you considered using pins with your party?" }
        p { +"A pin can be automatically assigned to one pair with each spin." }
        p { +"They're great for making sure regular tasks don't get forgotten." }
        p { +"Some teams have a 'production watch' pair, some might have an 'on-call' pair." }
        p { +"Maybe you decide that one pair should take out the trash." }
        p { +"Its like a little chore wheel for your team!" }
        h4 { +"Click on the pin bag button to give it a try!" }
    }
}
