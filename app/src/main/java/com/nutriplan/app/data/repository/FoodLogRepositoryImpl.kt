package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.FoodLogDao
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.data.mapper.toEntity
import com.nutriplan.app.domain.model.FoodLogEntry
import com.nutriplan.app.domain.repository.FoodLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Az étkezés-napló tároló megvalósítása a [FoodLogDao] felett.
 */
@Singleton
class FoodLogRepositoryImpl @Inject constructor(
    private val dao: FoodLogDao
) : FoodLogRepository {

    override fun entriesForDay(date: LocalDate): Flow<List<FoodLogEntry>> =
        dao.forDay(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override fun entriesForRange(from: LocalDate, to: LocalDate): Flow<List<FoodLogEntry>> =
        dao.forRange(from.toEpochDay(), to.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun rangeOnce(from: LocalDate, to: LocalDate): List<FoodLogEntry> =
        dao.forRangeOnce(from.toEpochDay(), to.toEpochDay()).map { it.toDomain() }

    override fun recent(): Flow<List<FoodLogEntry>> =
        dao.recentDistinct().map { list -> list.map { it.toDomain() } }

    override suspend fun add(entry: FoodLogEntry): Long = dao.insert(entry.toEntity())

    override suspend fun delete(id: Long) = dao.deleteById(id)
}
