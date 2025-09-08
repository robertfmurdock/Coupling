package com.zegreatrob.coupling.client.components.pairassignments

import com.zegreatrob.coupling.client.components.Reminder
import react.FC
import react.dom.html.ReactHTML

val PinReminder = FC {
    Reminder("pins-exist") {
        ReactHTML.h3 { +"Pins!" }
        ReactHTML.p { +"Have you considered using pins with your party?" }
        ReactHTML.p { +"A pin can be automatically assigned to one pair with each spin." }
        ReactHTML.p { +"They're great for making sure regular tasks don't get forgotten." }
        ReactHTML.p { +"Some teams have a 'production watch' pair, some might have an 'on-call' pair." }
        ReactHTML.p { +"Maybe you decide that one pair should take out the trash." }
        ReactHTML.p { +"Its like a little chore wheel for your team!" }
        ReactHTML.h4 { +"Click on the pin bag button to give it a try!" }
    }
}
