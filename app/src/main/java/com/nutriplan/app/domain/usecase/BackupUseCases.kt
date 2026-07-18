package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.repository.BackupRepository
import com.nutriplan.app.util.Logger
import javax.inject.Inject

/**
 * Mentés/visszatöltés use case-ek.
 */

/** Az összes adat JSON exportja szövegként. */
class ExportDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(): String {
        Logger.i(Logger.Tags.BACKUP, "ExportDataUseCase meghívva")
        return repository.exportToJson()
    }
}

/** Adatok visszatöltése JSON szövegből. */
class ImportDataUseCase @Inject constructor(
    private val repository: BackupRepository
) {
    suspend operator fun invoke(json: String) {
        Logger.i(Logger.Tags.BACKUP, "ImportDataUseCase meghívva")
        repository.importFromJson(json)
    }
}
