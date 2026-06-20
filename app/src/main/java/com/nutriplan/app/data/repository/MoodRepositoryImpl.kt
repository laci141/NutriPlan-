package com.nutriplan.app.data.repository

import com.nutriplan.app.data.local.dao.MoodDao
import com.nutriplan.app.data.mapper.toDomain
import com.nutriplan.app.data.mapper.toEntity
import com.nutriplan.app.domain.model.MoodEntry
import com.nutriplan.app.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A hangulat-napló tároló megvalósítása a [MoodDao] felett.
 */
@Singleton
class MoodRepositoryImpl @Inject constructor(
    private val dao: MoodDao
) : MoodRepository {

    override fun all(): Flow<List<MoodEntry>> =
        dao.all().map { list -> list.map { it.toDomain() } }

    override suspend fun set(entry: MoodEntry) = dao.upsert(entry.toEntity())

    override suspend fun deleteByDay(date: LocalDate) = dao.deleteByDay(date.toEpochDay())
}
