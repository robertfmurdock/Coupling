package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.json.GqlContributionWindow
import com.zegreatrob.coupling.stubmodel.stubContribution
import com.zegreatrob.coupling.stubmodel.stubPartyDetails
import com.zegreatrob.minassert.assertIsEqualTo
import com.zegreatrob.minassert.assertIsNotEqualTo
import com.zegreatrob.testmints.async.asyncSetup
import com.zegreatrob.wrapper.testinglibrary.react.RoleOptions
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.render
import com.zegreatrob.wrapper.testinglibrary.react.TestingLibraryReact.screen
import com.zegreatrob.wrapper.testinglibrary.userevent.UserEvent
import kotlin.test.Test

class ContributionListContentTest {

    @Test
    fun selectingLabelWillOnlyShowContributionsWithLabel() = asyncSetup(object {
        val expectedContribution1 = stubContribution().copy(label = "2")
        val expectedContribution2 = stubContribution().copy(label = "2")
        val excludedContribution1 = stubContribution().copy(label = "1")
        val excludedContribution2 = stubContribution().copy(label = "3")
        val contributions = listOf(
            excludedContribution1,
            expectedContribution1,
            excludedContribution2,
            expectedContribution2,
        )
        val actor = UserEvent.setup()
    }) {
        render(
            ContributionListContent.create(
                stubPartyDetails(),
                contributions,
                emptyList(),
                GqlContributionWindow.All,
                {},
            ),
        )
    } exercise {
        val labelFilterComboBox = screen.getByRole("combobox", RoleOptions(name = "Label Filter"))
        actor.selectOptions(labelFilterComboBox, "2")
    } verify {
        screen.queryByText(expectedContribution1.id.asShortId())
            .assertIsNotEqualTo(null)
        screen.queryByText(expectedContribution2.id.asShortId())
            .assertIsNotEqualTo(null)
        screen.queryByText(excludedContribution1.id.asShortId())
            .assertIsEqualTo(null)
        screen.queryByText(excludedContribution2.id.asShortId())
            .assertIsEqualTo(null)
    }
}
