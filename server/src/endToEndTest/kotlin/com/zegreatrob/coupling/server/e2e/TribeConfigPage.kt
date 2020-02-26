package com.zegreatrob.coupling.server.e2e

import com.zegreatrob.coupling.model.tribe.TribeId
import com.zegreatrob.coupling.server.e2e.external.protractor.By
import com.zegreatrob.coupling.server.e2e.external.protractor.ProtractorSyntax
import com.zegreatrob.coupling.server.e2e.external.protractor.element
import com.zegreatrob.coupling.server.e2e.external.protractor.waitToBePresent

object TribeConfigPage : ProtractorSyntax {

    val tribeConfigStyles = loadStyles("tribe/TribeConfig")
    val pageElement = tribeConfigStyles.element()
    val saveButton by tribeConfigStyles.getting()

    val tribeNameInput = element(By.id("tribe-name"))
    val tribeEmailInput = element(By.id("tribe-email"))
    val tribeIdInput = element(By.id("tribe-id"))
    val callSignCheckbox = element(By.id("call-sign-checkbox"))
    val badgeCheckbox = element(By.id("badge-checkbox"))
    val defaultBadgeNameInput = element(By.id("default-badge-name"))
    val altBadgeNameInput = element(By.id("alt-badge-name"))
    val differentBadgesOption = element(By.css("#pairing-rule option[label=\"Prefer Different Badges (Beta)\"]"))

    val deleteButton = element(By.className("delete-tribe-button"))
    val checkedOption = element(By.css("#pairing-rule option:checked"))

    suspend fun goTo(tribeId: TribeId) {
        setLocation("/${tribeId.value}/edit/")
        waitForPage()
    }

    suspend fun goToNew() {
        setLocation("/new-tribe/")
        waitForPage()
    }

    suspend fun waitForPage() {
        pageElement.waitToBePresent()
    }
}