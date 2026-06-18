package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.WeightDao
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.data.mapper.toEntity
import com.nutriplan.app.domain.model.WeightEntry
import com.nutriplan.app.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A testsúly-napló tároló megvalósítása a [WeightDao] felett.
 */
@Singleton
class WeightRepositoryImpl @Inject constructor(
    private val dao: WeightDao
) : WeightRepository {

    override fun all(): Flow<List<WeightEntry>> =
        dao.all().map { list -> list.map { it.toDomain() } }

    override suspend fun set(entry: WeightEntry) = dao.upsert(entry.toEntity())

    override suspend fun deleteByDay(date: LocalDate) = dao.deleteByDay(date.toEpochDay())
}
