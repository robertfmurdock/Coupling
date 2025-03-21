package com.zegreatrob.coupling.client.components.contribution

import com.zegreatrob.coupling.client.components.StubDispatcher
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
    fun byDefaultWillShowAllContributions() = asyncSetup(object {
        val contributions = listOf(
            stubContribution().copy(label = "1"),
            stubContribution().copy(label = "2"),
            stubContribution().copy(label = "3"),
            stubContribution().copy(label = "2"),
        )
    }) exercise {
        render(
            ContributionListContent.create(
                stubPartyDetails(),
                contributions,
                GqlContributionWindow.All,
                {},
                emptyList(),
                StubDispatcher().func(),
            ),
        )
    } verify {
        contributions.forEach { contribution ->
            screen.getByText(contribution.id.value.asShortId())
                .assertIsNotEqualTo(null)
        }
    }

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
                GqlContributionWindow.All,
                {},
                emptyList(),
                StubDispatcher().func(),
            ),
        )
    } exercise {
        val labelFilterComboBox = screen.getByRole("combobox", RoleOptions(name = "Label Filter"))
        actor.selectOptions(labelFilterComboBox, "2")
    } verify {
        screen.queryByText(expectedContribution1.id.value.asShortId())
            .assertIsNotEqualTo(null)
        screen.queryByText(expectedContribution2.id.value.asShortId())
            .assertIsNotEqualTo(null)
        screen.queryByText(excludedContribution1.id.value.asShortId())
            .assertIsEqualTo(null)
        screen.queryByText(excludedContribution2.id.value.asShortId())
            .assertIsEqualTo(null)
    }
}
