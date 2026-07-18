package com.nutriplan.app.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.nutriplan.app.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health Connect-alapú wearable (okosóra) szinkron.
 *
 * A Health Connect a rendszer közös egészségügyi adattára: a párosított okosóra
 * (Wear OS, Samsung Health, Fitbit, Garmin Connect stb.) ide írja a lépéseket és a
 * pulzust, mi pedig innen olvassuk – így a telefon hardveres szenzora helyett /
 * mellett az óra valós adatait tudjuk megjeleníteni.
 *
 * Minden hívás biztonságosan kezeli, ha a Health Connect nincs telepítve, vagy
 * nincs megadva engedély: ilyenkor null / üres értékkel tér vissza, nem dob hibát.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** A szükséges olvasási engedélyek (lépés + pulzus). */
    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    /** Telepítve és használható-e a Health Connect ezen az eszközön. */
    val isAvailable: Boolean
        get() = HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    /** Kell-e (és lehet-e) frissíteni a Health Connect szolgáltatást a Play áruházban. */
    val needsProviderUpdate: Boolean
        get() = HealthConnectClient.getSdkStatus(context) ==
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED

    private val client: HealthConnectClient? by lazy {
        if (isAvailable) runCatching { HealthConnectClient.getOrCreate(context) }
            .onFailure { Logger.w(Logger.Tags.VIEWMODEL, "Health Connect kliens hiba: ${it.message}") }
            .getOrNull()
        else null
    }

    /** Igaz, ha minden szükséges engedély meg van adva. */
    suspend fun hasAllPermissions(): Boolean {
        val c = client ?: return false
        return runCatching {
            c.permissionController.getGrantedPermissions().containsAll(permissions)
        }.getOrDefault(false)
    }

    /**
     * A mai nap összes lépése a Health Connect (wearable) forrásból.
     * null, ha nincs adat, engedély vagy elérhetőség.
     */
    suspend fun readTodaySteps(): Long? {
        val c = client ?: return null
        return runCatching {
            c.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = todayRange()
                )
            )[StepsRecord.COUNT_TOTAL]
        }.getOrNull()
    }

    /**
     * A mai nap legutóbbi pulzusértéke (bpm) – tipikusan az óra utolsó mérése.
     * null, ha nincs adat, engedély vagy elérhetőség.
     */
    suspend fun readLatestHeartRate(): Long? {
        val c = client ?: return null
        return runCatching {
            c.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = todayRange()
                )
            ).records
                .flatMap { it.samples }
                .maxByOrNull { it.time }
                ?.beatsPerMinute
        }.getOrNull()
    }

    private fun todayRange(): TimeRangeFilter {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant()
        return TimeRangeFilter.between(start, Instant.now())
    }
}
