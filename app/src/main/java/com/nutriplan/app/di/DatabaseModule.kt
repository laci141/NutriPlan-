package com.nutriplan.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nutriplan.app.data.local.NutriPlanDatabase
import com.nutriplan.app.data.local.dao.FoodLogDao
import com.nutriplan.app.data.local.dao.MealPlanDao
import com.nutriplan.app.data.local.dao.RecipeDao
import com.nutriplan.app.data.local.dao.ShoppingDao
import com.nutriplan.app.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt modul az adatbázis és a DAO-k biztosításához.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 3 -> 4: kedvenc oszlop hozzáadása a felhasználói adatok megőrzésével
    private val migration3to4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE recipes ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        }
    }

    // 4 -> 5: étkezés-napló tábla létrehozása (a meglévő adatok megőrzésével)
    private val migration4to5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS food_log (" +
                    "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    "epochDay INTEGER NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "calories INTEGER NOT NULL, " +
                    "protein REAL NOT NULL, " +
                    "carbs REAL NOT NULL, " +
                    "fat REAL NOT NULL, " +
                    "mealType TEXT NOT NULL)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_food_log_epochDay ON food_log(epochDay)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NutriPlanDatabase {
        Logger.i(Logger.Tags.DATABASE, "Database initialized – Room adatbázis példány létrehozása")
        return Room.databaseBuilder(
            context,
            NutriPlanDatabase::class.java,
            NutriPlanDatabase.DATABASE_NAME
        )
            .addMigrations(migration3to4, migration4to5)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRecipeDao(database: NutriPlanDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideMealPlanDao(database: NutriPlanDatabase): MealPlanDao = database.mealPlanDao()

    @Provides
    fun provideShoppingDao(database: NutriPlanDatabase): ShoppingDao = database.shoppingDao()

    @Provides
    fun provideFoodLogDao(database: NutriPlanDatabase): FoodLogDao = database.foodLogDao()
}
