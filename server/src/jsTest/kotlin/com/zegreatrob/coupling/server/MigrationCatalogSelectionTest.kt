package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.repository.dynamo.MigrationLedgerPhase
import com.zegreatrob.coupling.repository.dynamo.MigrationMode
import com.zegreatrob.minassert.assertIsEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MigrationCatalogSelectionTest {

    @Test
    fun parsesEverySupportedMigrationMode() {
        MigrationMode.parse("disabled").assertIsEqualTo(MigrationMode.DISABLED)
        MigrationMode.parse("dual_write").assertIsEqualTo(MigrationMode.DUAL_WRITE)
        MigrationMode.parse("cutover").assertIsEqualTo(MigrationMode.CUTOVER)
    }

    @Test
    fun rejectsMissingMigrationMode() {
        assertFailsWith<IllegalStateException> { MigrationMode.parse(null) }
    }

    @Test
    fun rejectsUnknownMigrationMode() {
        assertFailsWith<IllegalStateException> { MigrationMode.parse("preview") }
    }

    @Test
    fun disabledModeWithoutTargetSchemaSelectsLegacyCatalog() {
        migrationCatalogSelection(MigrationMode.DISABLED, null)
            .assertIsEqualTo(MigrationCatalogSelection.LEGACY)
    }

    @Test
    fun disabledModeWithDisabledLedgerSelectsLegacyCatalog() {
        migrationCatalogSelection(MigrationMode.DISABLED, MigrationLedgerPhase.DISABLED)
            .assertIsEqualTo(MigrationCatalogSelection.LEGACY)
    }

    @Test
    fun dualWriteModeWithDisabledLedgerSelectsLegacyCatalog() {
        migrationCatalogSelection(MigrationMode.DUAL_WRITE, MigrationLedgerPhase.DISABLED)
            .assertIsEqualTo(MigrationCatalogSelection.LEGACY)
    }

    @Test
    fun disabledModeWithDualWriteLedgerSelectsLegacyCatalog() {
        migrationCatalogSelection(MigrationMode.DISABLED, MigrationLedgerPhase.DUAL_WRITE)
            .assertIsEqualTo(MigrationCatalogSelection.LEGACY)
    }

    @Test
    fun disabledModeWithCutoverLedgerIsRejected() {
        assertFailsWith<IllegalStateException> {
            migrationCatalogSelection(MigrationMode.DISABLED, MigrationLedgerPhase.CUTOVER)
        }
    }

    @Test
    fun cutoverModeWithoutCutoverLedgerIsRejected() {
        assertFailsWith<IllegalStateException> {
            migrationCatalogSelection(MigrationMode.CUTOVER, MigrationLedgerPhase.DISABLED)
        }
    }
}
