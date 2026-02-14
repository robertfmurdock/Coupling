package com.zegreatrob.coupling.client.components.stats

import com.zegreatrob.coupling.client.components.contribution.contributionContentBackgroundColor
import com.zegreatrob.minreact.ReactFunc
import com.zegreatrob.minreact.nfc
import js.objects.unsafeJso
import react.Key
import react.Props
import react.ReactNode
import react.dom.events.ChangeEvent
import react.dom.html.ReactHTML.option
import web.html.HTMLSelectElement

external interface ContributionLabelFilterProps : Props {
    var allLabels: Set<String>
    var selected: String?
    var setSelected: (String?) -> Unit
}

@ReactFunc
val ContributionLabelFilter by nfc<ContributionLabelFilterProps> { props ->
    val (allLabels, selected, setSelected) = props
    CouplingSelect {
        label = ReactNode("Label Filter")
        backgroundColor = contributionContentBackgroundColor
        selectProps = unsafeJso {
            value = selected
            disabled = allLabels.size <= 1
            onChange = { event -> setSelected(event.handlePlaceholder()) }
        }
        option {
            value = NULL_PLACEHOLDER
            +"All Labels"
        }
        allLabels.forEach { label ->
            option {
                key = Key(label)
                value = label
                +label
            }
        }
    }
}

private const val NULL_PLACEHOLDER = "NULL"

private fun ChangeEvent<*, HTMLSelectElement>.handlePlaceholder() = target.value.let {
    if (it == NULL_PLACEHOLDER) null else it
}
