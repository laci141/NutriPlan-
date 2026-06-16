package com.nutriplan.app.di

import com.nutriplan.app.data.repository.BackupRepositoryImpl
import com.nutriplan.app.data.repository.MealPlanRepositoryImpl
import com.nutriplan.app.data.repository.RecipeRepositoryImpl
import com.nutriplan.app.data.repository.ShoppingRepositoryImpl
import com.nutriplan.app.domain.repository.BackupRepository
import com.nutriplan.app.domain.repository.MealPlanRepository
import com.nutriplan.app.domain.repository.RecipeRepository
import com.nutriplan.app.domain.repository.ShoppingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt modul a tároló interfészek és a konkrét megvalósítások összekötéséhez.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    @Binds
    @Singleton
    abstract fun bindMealPlanRepository(impl: MealPlanRepositoryImpl): MealPlanRepository

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
