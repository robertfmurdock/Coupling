package com.zegreatrob.coupling.server

import com.zegreatrob.coupling.repository.dynamo.MigrationLedgerPhase
import com.zegreatrob.coupling.repository.dynamo.MigrationMode

enum class MigrationCatalogSelection {
    LEGACY,
}

fun migrationCatalogSelection(
    requestedMode: MigrationMode,
    ledgerPhase: MigrationLedgerPhase?,
): MigrationCatalogSelection = when (requestedMode) {
    MigrationMode.DISABLED -> when (ledgerPhase) {
        MigrationLedgerPhase.CUTOVER -> throw IllegalStateException("Target catalog is not implemented")
        else -> MigrationCatalogSelection.LEGACY
    }

    MigrationMode.DUAL_WRITE -> when (ledgerPhase) {
        MigrationLedgerPhase.DISABLED,
        MigrationLedgerPhase.BACKFILLING,
        -> MigrationCatalogSelection.LEGACY

        MigrationLedgerPhase.DUAL_WRITE,
        MigrationLedgerPhase.CUTOVER,
        -> throw IllegalStateException("Migration catalog is not implemented")

        null -> throw IllegalStateException("Migration ledger is required for dual-write mode")
    }

    MigrationMode.CUTOVER -> when (ledgerPhase) {
        MigrationLedgerPhase.CUTOVER -> throw IllegalStateException("Target catalog is not implemented")
        else -> throw IllegalStateException("Cutover requires a reconciled dual-write ledger")
    }
}
