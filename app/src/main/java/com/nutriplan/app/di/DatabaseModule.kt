package com.nutriplan.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nutriplan.app.data.local.NutriPlanDatabase
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NutriPlanDatabase {
        Logger.i(Logger.Tags.DATABASE, "Database initialized – Room adatbázis példány létrehozása")
        return Room.databaseBuilder(
            context,
            NutriPlanDatabase::class.java,
            NutriPlanDatabase.DATABASE_NAME
        )
            .addMigrations(migration3to4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideRecipeDao(database: NutriPlanDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideMealPlanDao(database: NutriPlanDatabase): MealPlanDao = database.mealPlanDao()

    @Provides
    fun provideShoppingDao(database: NutriPlanDatabase): ShoppingDao = database.shoppingDao()
}
